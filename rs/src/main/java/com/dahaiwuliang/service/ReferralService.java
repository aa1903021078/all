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

/**
 * Referral state machine:
 *   DRAFT → REVIEWING → ACCEPTED → VISITING → COMPLETED
 *                    ↘ REJECTED
 */
@Service
@RequiredArgsConstructor
public class ReferralService {

    private final ReferralMapper referralMapper;
    private final SysUserMapper userMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;
    private final ScheduleMapper scheduleMapper;
    private final AppointmentMapper appointmentMapper;
    private final ScheduleService scheduleService;
    private final NotificationService notify;

    @Transactional
    public Referral create(Long doctorId, Referral input) {
        SysUser doctor = userMapper.selectById(doctorId);
        if (doctor == null || !"DOCTOR".equals(doctor.getRole())) throw new BizException(403, "仅医生可发起转诊");
        if (input.getPatientId() == null) throw new BizException(400, "缺少患者");
        if (input.getToHospitalId() == null) throw new BizException(400, "缺少目标机构");
        if (input.getType() == null) throw new BizException(400, "缺少转诊类型");

        Referral r = new Referral();
        r.setPatientId(input.getPatientId());
        r.setFromDoctorId(doctorId);
        r.setFromHospitalId(doctor.getHospitalId());
        r.setFromDeptId(doctor.getDeptId());
        r.setToHospitalId(input.getToHospitalId());
        r.setToDeptId(input.getToDeptId());
        r.setType(input.getType());
        r.setSummary(input.getSummary());
        r.setRehabPlan(input.getRehabPlan());
        r.setImageUrl(input.getImageUrl());
        r.setStatus("REVIEWING");
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        referralMapper.insert(r);

        notify.push(input.getPatientId(), "转诊申请已提交",
                "医生已为您发起" + ("UP".equals(input.getType()) ? "上转" : "下转") + "申请，等待目标机构审核。",
                "REFERRAL_NEW");
        return r;
    }

    @Transactional
    public Referral approve(Long doctorId, Long referralId, Long targetScheduleId) {
        Referral r = referralMapper.selectById(referralId);
        if (r == null) throw new BizException(404, "转诊单不存在");
        SysUser doctor = userMapper.selectById(doctorId);
        if (doctor == null) throw new BizException(403, "无权操作");
        // must belong to receiving hospital (or admin via separate path)
        if (!"ADMIN".equals(doctor.getRole())
                && !Objects.equals(doctor.getHospitalId(), r.getToHospitalId()))
            throw new BizException(403, "仅接收方医院可审核");
        if (!"REVIEWING".equals(r.getStatus())) throw new BizException(400, "状态不允许审核");
        if (targetScheduleId == null) throw new BizException(400, "请指定目标号源");

        Schedule s = scheduleMapper.selectById(targetScheduleId);
        if (s == null) throw new BizException(404, "目标号源不存在");
        if (!Objects.equals(s.getHospitalId(), r.getToHospitalId()))
            throw new BizException(400, "目标号源不属于接收方医院");

        // lock quota
        scheduleService.decrementQuota(targetScheduleId);

        // create appointment for the patient at target schedule
        Appointment a = new Appointment();
        a.setPatientId(r.getPatientId());
        a.setScheduleId(targetScheduleId);
        a.setDoctorId(s.getDoctorId());
        a.setHospitalId(s.getHospitalId());
        a.setDeptId(s.getDeptId());
        a.setStatus("BOOKED");
        a.setSource("REFERRAL");
        a.setReferralId(r.getId());
        a.setCreatedAt(LocalDateTime.now());
        appointmentMapper.insert(a);

        r.setStatus("ACCEPTED");
        r.setToDoctorId(s.getDoctorId());
        r.setToDeptId(s.getDeptId());
        r.setTargetScheduleId(targetScheduleId);
        r.setTargetAppointmentId(a.getId());
        r.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(r);

        notify.push(r.getPatientId(), "转诊已受理",
                "您的转诊申请已被接收方医院受理，请按预约时间前往就诊。",
                "REFERRAL_ACCEPTED");
        return r;
    }

    @Transactional
    public Referral reject(Long doctorId, Long referralId, String reason) {
        Referral r = referralMapper.selectById(referralId);
        if (r == null) throw new BizException(404, "转诊单不存在");
        SysUser doctor = userMapper.selectById(doctorId);
        if (doctor == null) throw new BizException(403, "无权操作");
        if (!"ADMIN".equals(doctor.getRole())
                && !Objects.equals(doctor.getHospitalId(), r.getToHospitalId()))
            throw new BizException(403, "仅接收方医院可审核");
        if (!"REVIEWING".equals(r.getStatus())) throw new BizException(400, "状态不允许审核");
        r.setStatus("REJECTED");
        r.setRejectReason(reason);
        r.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(r);

        notify.push(r.getPatientId(), "转诊被驳回",
                "您的转诊申请被接收方医院驳回。原因：" + (reason == null ? "未填写" : reason),
                "REFERRAL_REJECTED");
        return r;
    }

    @Transactional
    public Referral updateStatus(Long referralId, String status) {
        Referral r = referralMapper.selectById(referralId);
        if (r == null) throw new BizException(404, "转诊单不存在");
        Set<String> allowed = new HashSet<>(Arrays.asList(
                "DRAFT", "REVIEWING", "ACCEPTED", "VISITING", "COMPLETED", "REJECTED"));
        if (!allowed.contains(status)) throw new BizException(400, "非法状态");
        r.setStatus(status);
        r.setUpdatedAt(LocalDateTime.now());
        referralMapper.updateById(r);
        notify.push(r.getPatientId(), "转诊状态更新",
                "您的转诊单状态已更新为：" + status, "REFERRAL_STATUS");
        return r;
    }

    public List<Map<String, Object>> listByPatient(Long patientId) {
        return enrich(referralMapper.selectList(Wrappers.<Referral>lambdaQuery()
                .eq(Referral::getPatientId, patientId).orderByDesc(Referral::getCreatedAt)));
    }

    public List<Map<String, Object>> listOutgoing(Long doctorId) {
        return enrich(referralMapper.selectList(Wrappers.<Referral>lambdaQuery()
                .eq(Referral::getFromDoctorId, doctorId).orderByDesc(Referral::getCreatedAt)));
    }

    public List<Map<String, Object>> listIncoming(Long hospitalId) {
        return enrich(referralMapper.selectList(Wrappers.<Referral>lambdaQuery()
                .eq(Referral::getToHospitalId, hospitalId).orderByDesc(Referral::getCreatedAt)));
    }

    public List<Map<String, Object>> listAll(String status, Long hospitalId) {
        return enrich(referralMapper.selectList(Wrappers.<Referral>lambdaQuery()
                .eq(status != null && !status.isEmpty(), Referral::getStatus, status)
                .and(hospitalId != null, w -> w.eq(Referral::getFromHospitalId, hospitalId)
                        .or().eq(Referral::getToHospitalId, hospitalId))
                .orderByDesc(Referral::getCreatedAt)));
    }

    private List<Map<String, Object>> enrich(List<Referral> list) {
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> uids = new HashSet<>(), hids = new HashSet<>(), dids = new HashSet<>();
        for (Referral r : list) {
            if (r.getPatientId() != null) uids.add(r.getPatientId());
            if (r.getFromDoctorId() != null) uids.add(r.getFromDoctorId());
            if (r.getToDoctorId() != null) uids.add(r.getToDoctorId());
            if (r.getFromHospitalId() != null) hids.add(r.getFromHospitalId());
            if (r.getToHospitalId() != null) hids.add(r.getToHospitalId());
            if (r.getFromDeptId() != null) dids.add(r.getFromDeptId());
            if (r.getToDeptId() != null) dids.add(r.getToDeptId());
        }
        Map<Long, SysUser> users = new HashMap<>();
        if (!uids.isEmpty()) userMapper.selectBatchIds(uids).forEach(u -> users.put(u.getId(), u));
        Map<Long, Hospital> hs = new HashMap<>();
        if (!hids.isEmpty()) hospitalMapper.selectBatchIds(hids).forEach(h -> hs.put(h.getId(), h));
        Map<Long, Department> ds = new HashMap<>();
        if (!dids.isEmpty()) deptMapper.selectBatchIds(dids).forEach(d -> ds.put(d.getId(), d));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Referral r : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("type", r.getType());
            m.put("status", r.getStatus());
            m.put("summary", r.getSummary());
            m.put("rehabPlan", r.getRehabPlan());
            m.put("imageUrl", r.getImageUrl());
            m.put("rejectReason", r.getRejectReason());
            m.put("createdAt", r.getCreatedAt());
            m.put("updatedAt", r.getUpdatedAt());
            m.put("targetAppointmentId", r.getTargetAppointmentId());
            m.put("patientId", r.getPatientId());
            m.put("patientName", Optional.ofNullable(users.get(r.getPatientId())).map(SysUser::getRealName).orElse(""));
            m.put("fromDoctorId", r.getFromDoctorId());
            m.put("fromDoctorName", Optional.ofNullable(users.get(r.getFromDoctorId())).map(SysUser::getRealName).orElse(""));
            m.put("fromHospitalId", r.getFromHospitalId());
            m.put("fromHospitalName", Optional.ofNullable(hs.get(r.getFromHospitalId())).map(Hospital::getName).orElse(""));
            m.put("fromDeptName", Optional.ofNullable(ds.get(r.getFromDeptId())).map(Department::getName).orElse(""));
            m.put("toHospitalId", r.getToHospitalId());
            m.put("toHospitalName", Optional.ofNullable(hs.get(r.getToHospitalId())).map(Hospital::getName).orElse(""));
            m.put("toDeptName", Optional.ofNullable(ds.get(r.getToDeptId())).map(Department::getName).orElse(""));
            m.put("toDoctorName", Optional.ofNullable(users.get(r.getToDoctorId())).map(SysUser::getRealName).orElse(""));
            out.add(m);
        }
        return out;
    }
}
