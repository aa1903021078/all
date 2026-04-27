package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.common.BizException;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final ScheduleMapper scheduleMapper;
    private final SysUserMapper userMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;
    private final MedicalRecordMapper recordMapper;
    private final ScheduleService scheduleService;
    private final NotificationService notify;

    @Transactional
    public Appointment book(Long patientId, Long scheduleId) {
        Schedule s = scheduleMapper.selectById(scheduleId);
        if (s == null) throw new BizException(404, "排班不存在");
        // duplicate check
        Long dup = appointmentMapper.selectCount(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getPatientId, patientId)
                .eq(Appointment::getScheduleId, scheduleId)
                .eq(Appointment::getStatus, "BOOKED"));
        if (dup != null && dup > 0) throw new BizException(400, "您已预约该号源");

        scheduleService.decrementQuota(scheduleId); // optimistic lock here

        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setScheduleId(scheduleId);
        a.setDoctorId(s.getDoctorId());
        a.setHospitalId(s.getHospitalId());
        a.setDeptId(s.getDeptId());
        a.setStatus("BOOKED");
        a.setSource("SELF");
        a.setCreatedAt(LocalDateTime.now());
        appointmentMapper.insert(a);

        notify.push(patientId, "预约成功",
                "您已成功预约 " + s.getWorkDate() + " " + s.getTimeSlot() + " 的号源。", "APPOINTMENT_OK");
        return a;
    }

    @Transactional
    public void cancel(Long patientId, Long appointmentId) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw new BizException(404, "预约不存在");
        if (!a.getPatientId().equals(patientId)) throw new BizException(403, "无权操作");
        if (!"BOOKED".equals(a.getStatus())) throw new BizException(400, "当前状态不可取消");
        a.setStatus("CANCELLED");
        appointmentMapper.updateById(a);
        scheduleService.incrementQuota(a.getScheduleId());
    }

    public List<Map<String, Object>> listByPatient(Long patientId) {
        List<Appointment> list = appointmentMapper.selectList(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getPatientId, patientId)
                .orderByDesc(Appointment::getCreatedAt));
        return enrich(list, true);
    }

    public List<Map<String, Object>> listByDoctor(Long doctorId, String status) {
        List<Appointment> list = appointmentMapper.selectList(Wrappers.<Appointment>lambdaQuery()
                .eq(Appointment::getDoctorId, doctorId)
                .eq(status != null && !status.isEmpty(), Appointment::getStatus, status)
                .orderByDesc(Appointment::getCreatedAt));
        return enrich(list, false);
    }

    private List<Map<String, Object>> enrich(List<Appointment> list, boolean forPatient) {
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> userIds = new HashSet<>();
        Set<Long> hids = new HashSet<>();
        Set<Long> dids = new HashSet<>();
        Set<Long> sids = new HashSet<>();
        Set<Long> aids = new HashSet<>();
        for (Appointment a : list) {
            userIds.add(a.getDoctorId());
            userIds.add(a.getPatientId());
            hids.add(a.getHospitalId());
            dids.add(a.getDeptId());
            sids.add(a.getScheduleId());
            aids.add(a.getId());
        }
        Map<Long, SysUser> users = new HashMap<>();
        userMapper.selectBatchIds(userIds).forEach(u -> users.put(u.getId(), u));
        Map<Long, Hospital> hospitals = new HashMap<>();
        hospitalMapper.selectBatchIds(hids).forEach(h -> hospitals.put(h.getId(), h));
        Map<Long, Department> depts = new HashMap<>();
        deptMapper.selectBatchIds(dids).forEach(d -> depts.put(d.getId(), d));
        Map<Long, Schedule> schedules = new HashMap<>();
        scheduleMapper.selectBatchIds(sids).forEach(s -> schedules.put(s.getId(), s));
        Map<Long, MedicalRecord> records = new HashMap<>();
        recordMapper.selectList(Wrappers.<MedicalRecord>lambdaQuery().in(MedicalRecord::getAppointmentId, aids))
                .forEach(r -> records.put(r.getAppointmentId(), r));

        List<Map<String, Object>> out = new ArrayList<>();
        for (Appointment a : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("status", a.getStatus());
            m.put("source", a.getSource());
            m.put("createdAt", a.getCreatedAt());
            m.put("scheduleId", a.getScheduleId());
            Schedule s = schedules.get(a.getScheduleId());
            if (s != null) {
                m.put("workDate", s.getWorkDate());
                m.put("timeSlot", s.getTimeSlot());
            }
            m.put("doctorId", a.getDoctorId());
            m.put("doctorName", Optional.ofNullable(users.get(a.getDoctorId())).map(SysUser::getRealName).orElse(""));
            m.put("doctorTitle", Optional.ofNullable(users.get(a.getDoctorId())).map(SysUser::getTitle).orElse(""));
            m.put("patientId", a.getPatientId());
            SysUser p = users.get(a.getPatientId());
            if (p != null) {
                m.put("patientName", p.getRealName());
                m.put("patientGender", p.getGender());
                m.put("patientPhone", p.getPhone());
            }
            m.put("hospitalName", Optional.ofNullable(hospitals.get(a.getHospitalId())).map(Hospital::getName).orElse(""));
            m.put("deptName", Optional.ofNullable(depts.get(a.getDeptId())).map(Department::getName).orElse(""));
            m.put("hasRecord", records.containsKey(a.getId()));
            if (forPatient && records.containsKey(a.getId())) {
                m.put("record", records.get(a.getId()));
            }
            out.add(m);
        }
        return out;
    }
}
