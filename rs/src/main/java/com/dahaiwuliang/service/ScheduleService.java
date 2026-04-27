package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.common.BizException;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final AppointmentMapper appointmentMapper;
    private final SysUserMapper userMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;
    private final NotificationService notify;

    public List<Map<String, Object>> listForBooking(Long hospitalId, Long deptId, LocalDate from) {
        LambdaQueryWrapper<Schedule> w = Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getStatus, "OPEN")
                .ge(Schedule::getWorkDate, from == null ? LocalDate.now() : from)
                .orderByAsc(Schedule::getWorkDate)
                .orderByAsc(Schedule::getTimeSlot);
        if (hospitalId != null) w.eq(Schedule::getHospitalId, hospitalId);
        if (deptId != null) w.eq(Schedule::getDeptId, deptId);
        List<Schedule> list = scheduleMapper.selectList(w);
        return enrich(list);
    }

    public List<Map<String, Object>> listByDoctor(Long doctorId) {
        List<Schedule> list = scheduleMapper.selectList(Wrappers.<Schedule>lambdaQuery()
                .eq(Schedule::getDoctorId, doctorId)
                .orderByDesc(Schedule::getWorkDate));
        return enrich(list);
    }

    public List<Map<String, Object>> listAll() {
        return enrich(scheduleMapper.selectList(Wrappers.<Schedule>lambdaQuery()
                .orderByDesc(Schedule::getWorkDate)));
    }

    private List<Map<String, Object>> enrich(List<Schedule> list) {
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> doctorIds = new HashSet<>();
        Set<Long> hospitalIds = new HashSet<>();
        Set<Long> deptIds = new HashSet<>();
        for (Schedule s : list) {
            doctorIds.add(s.getDoctorId());
            hospitalIds.add(s.getHospitalId());
            deptIds.add(s.getDeptId());
        }
        Map<Long, SysUser> doctors = new HashMap<>();
        userMapper.selectBatchIds(doctorIds).forEach(u -> doctors.put(u.getId(), u));
        Map<Long, Hospital> hospitals = new HashMap<>();
        hospitalMapper.selectBatchIds(hospitalIds).forEach(h -> hospitals.put(h.getId(), h));
        Map<Long, Department> depts = new HashMap<>();
        deptMapper.selectBatchIds(deptIds).forEach(d -> depts.put(d.getId(), d));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Schedule s : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("doctorId", s.getDoctorId());
            m.put("doctorName", Optional.ofNullable(doctors.get(s.getDoctorId())).map(SysUser::getRealName).orElse(""));
            m.put("doctorTitle", Optional.ofNullable(doctors.get(s.getDoctorId())).map(SysUser::getTitle).orElse(""));
            m.put("deptId", s.getDeptId());
            m.put("deptName", Optional.ofNullable(depts.get(s.getDeptId())).map(Department::getName).orElse(""));
            m.put("hospitalId", s.getHospitalId());
            m.put("hospitalName", Optional.ofNullable(hospitals.get(s.getHospitalId())).map(Hospital::getName).orElse(""));
            m.put("workDate", s.getWorkDate());
            m.put("timeSlot", s.getTimeSlot());
            m.put("totalQuota", s.getTotalQuota());
            m.put("remainingQuota", s.getRemainingQuota());
            m.put("status", s.getStatus());
            m.put("version", s.getVersion());
            out.add(m);
        }
        return out;
    }

    @Transactional
    public Schedule create(Schedule s) {
        if (s.getDoctorId() == null) throw new BizException(400, "缺少医生");
        if (s.getWorkDate() == null) throw new BizException(400, "缺少出诊日期");
        SysUser doc = userMapper.selectById(s.getDoctorId());
        if (doc == null || !"DOCTOR".equals(doc.getRole())) throw new BizException(400, "医生不存在");
        s.setHospitalId(doc.getHospitalId());
        s.setDeptId(doc.getDeptId());
        if (s.getTotalQuota() == null) s.setTotalQuota(20);
        s.setRemainingQuota(s.getTotalQuota());
        s.setStatus("OPEN");
        s.setVersion(0);
        s.setCreatedAt(LocalDateTime.now());
        scheduleMapper.insert(s);
        return s;
    }

    @Transactional
    public Schedule update(Long id, Schedule patch) {
        Schedule s = scheduleMapper.selectById(id);
        if (s == null) throw new BizException(404, "排班不存在");
        if (patch.getTotalQuota() != null) {
            int diff = patch.getTotalQuota() - s.getTotalQuota();
            s.setTotalQuota(patch.getTotalQuota());
            s.setRemainingQuota(Math.max(0, s.getRemainingQuota() + diff));
        }
        if (patch.getStatus() != null) s.setStatus(patch.getStatus());
        if (patch.getTimeSlot() != null) s.setTimeSlot(patch.getTimeSlot());
        if (patch.getWorkDate() != null) s.setWorkDate(patch.getWorkDate());
        if (scheduleMapper.updateById(s) == 0) {
            throw new BizException(409, "并发冲突，请重试");
        }
        return s;
    }

    @Transactional
    public void close(Long id, String reason) {
        Schedule s = scheduleMapper.selectById(id);
        if (s == null) throw new BizException(404, "排班不存在");
        s.setStatus("CLOSED");
        if (scheduleMapper.updateById(s) == 0) throw new BizException(409, "并发冲突，请重试");
        // notify booked patients
        List<Appointment> apps = appointmentMapper.selectList(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getScheduleId, id).eq(Appointment::getStatus, "BOOKED"));
        for (Appointment a : apps) {
            notify.push(a.getPatientId(), "您的预约已停诊",
                    "排班 " + s.getWorkDate() + " " + s.getTimeSlot() + " 已停诊，请重新预约。"
                            + (reason == null ? "" : " 原因：" + reason),
                    "SCHEDULE_CLOSED");
            a.setStatus("CANCELLED");
            appointmentMapper.updateById(a);
        }
    }

    /** Optimistic-lock decrement remaining quota. Returns the schedule, or throws on conflict / no quota. */
    @Transactional
    public Schedule decrementQuota(Long scheduleId) {
        for (int attempt = 0; attempt < 3; attempt++) {
            Schedule s = scheduleMapper.selectById(scheduleId);
            if (s == null) throw new BizException(404, "排班不存在");
            if (!"OPEN".equals(s.getStatus())) throw new BizException(400, "该排班已关闭");
            if (s.getRemainingQuota() == null || s.getRemainingQuota() <= 0)
                throw new BizException(400, "号源已约满");
            int original = s.getRemainingQuota();
            s.setRemainingQuota(original - 1);
            int n = scheduleMapper.updateById(s); // @Version triggers optimistic lock
            if (n == 1) return s;
        }
        throw new BizException(409, "号源被他人抢占，请重试");
    }

    @Transactional
    public void incrementQuota(Long scheduleId) {
        for (int attempt = 0; attempt < 3; attempt++) {
            Schedule s = scheduleMapper.selectById(scheduleId);
            if (s == null) return;
            s.setRemainingQuota((s.getRemainingQuota() == null ? 0 : s.getRemainingQuota()) + 1);
            if (scheduleMapper.updateById(s) == 1) return;
        }
    }
}
