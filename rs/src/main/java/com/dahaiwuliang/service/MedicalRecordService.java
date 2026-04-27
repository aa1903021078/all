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
public class MedicalRecordService {

    private final MedicalRecordMapper recordMapper;
    private final AppointmentMapper appointmentMapper;
    private final SysUserMapper userMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper deptMapper;

    @Transactional
    public MedicalRecord diagnose(Long doctorId, Long appointmentId, MedicalRecord input) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw new BizException(404, "预约不存在");
        if (!a.getDoctorId().equals(doctorId)) throw new BizException(403, "无权操作他人接诊");
        MedicalRecord existed = recordMapper.selectOne(Wrappers.<MedicalRecord>lambdaQuery()
                .eq(MedicalRecord::getAppointmentId, appointmentId));
        MedicalRecord r = existed == null ? new MedicalRecord() : existed;
        r.setAppointmentId(appointmentId);
        r.setPatientId(a.getPatientId());
        r.setDoctorId(doctorId);
        r.setHospitalId(a.getHospitalId());
        r.setDeptId(a.getDeptId());
        if (input.getChiefComplaint() != null) r.setChiefComplaint(input.getChiefComplaint());
        if (input.getDiagnosis() != null) r.setDiagnosis(input.getDiagnosis());
        if (input.getPrescription() != null) r.setPrescription(input.getPrescription());
        if (input.getAdvice() != null) r.setAdvice(input.getAdvice());
        if (input.getImageUrl() != null) r.setImageUrl(input.getImageUrl());
        if (existed == null) {
            r.setCreatedAt(LocalDateTime.now());
            recordMapper.insert(r);
        } else {
            recordMapper.updateById(r);
        }
        // 更新预约状态为已就诊
        a.setStatus("VISITED");
        appointmentMapper.updateById(a);
        return r;
    }

    public List<Map<String, Object>> listByPatient(Long patientId) {
        List<MedicalRecord> list = recordMapper.selectList(Wrappers.<MedicalRecord>lambdaQuery()
                .eq(MedicalRecord::getPatientId, patientId)
                .orderByDesc(MedicalRecord::getCreatedAt));
        if (list.isEmpty()) return Collections.emptyList();
        Set<Long> uids = new HashSet<>(), hids = new HashSet<>(), dids = new HashSet<>();
        for (MedicalRecord r : list) { uids.add(r.getDoctorId()); uids.add(r.getPatientId());
            hids.add(r.getHospitalId()); dids.add(r.getDeptId()); }
        Map<Long, SysUser> users = new HashMap<>();
        userMapper.selectBatchIds(uids).forEach(u -> users.put(u.getId(), u));
        Map<Long, Hospital> hospitals = new HashMap<>();
        hospitalMapper.selectBatchIds(hids).forEach(h -> hospitals.put(h.getId(), h));
        Map<Long, Department> depts = new HashMap<>();
        deptMapper.selectBatchIds(dids).forEach(d -> depts.put(d.getId(), d));
        List<Map<String, Object>> out = new ArrayList<>();
        for (MedicalRecord r : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", r.getId());
            m.put("appointmentId", r.getAppointmentId());
            m.put("chiefComplaint", r.getChiefComplaint());
            m.put("diagnosis", r.getDiagnosis());
            m.put("prescription", r.getPrescription());
            m.put("advice", r.getAdvice());
            m.put("imageUrl", r.getImageUrl());
            m.put("createdAt", r.getCreatedAt());
            m.put("doctorName", Optional.ofNullable(users.get(r.getDoctorId())).map(SysUser::getRealName).orElse(""));
            m.put("doctorTitle", Optional.ofNullable(users.get(r.getDoctorId())).map(SysUser::getTitle).orElse(""));
            m.put("patientName", Optional.ofNullable(users.get(r.getPatientId())).map(SysUser::getRealName).orElse(""));
            m.put("patientGender", Optional.ofNullable(users.get(r.getPatientId())).map(SysUser::getGender).orElse(""));
            m.put("hospitalName", Optional.ofNullable(hospitals.get(r.getHospitalId())).map(Hospital::getName).orElse(""));
            m.put("deptName", Optional.ofNullable(depts.get(r.getDeptId())).map(Department::getName).orElse(""));
            out.add(m);
        }
        return out;
    }

    public Map<String, Object> getByAppointment(Long appointmentId, Long currentUserId, String role) {
        Appointment a = appointmentMapper.selectById(appointmentId);
        if (a == null) throw new BizException(404, "预约不存在");
        if ("PATIENT".equals(role) && !a.getPatientId().equals(currentUserId))
            throw new BizException(403, "无权查看");
        if ("DOCTOR".equals(role) && !a.getDoctorId().equals(currentUserId))
            throw new BizException(403, "无权查看");
        MedicalRecord r = recordMapper.selectOne(Wrappers.<MedicalRecord>lambdaQuery()
                .eq(MedicalRecord::getAppointmentId, appointmentId));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("appointment", a);
        m.put("record", r);
        m.put("patient", Optional.ofNullable(userMapper.selectById(a.getPatientId())).map(this::safe).orElse(null));
        m.put("doctor", Optional.ofNullable(userMapper.selectById(a.getDoctorId())).map(this::safe).orElse(null));
        m.put("hospital", hospitalMapper.selectById(a.getHospitalId()));
        m.put("department", deptMapper.selectById(a.getDeptId()));
        // history records of this patient (for doctor reference)
        List<MedicalRecord> history = recordMapper.selectList(Wrappers.<MedicalRecord>lambdaQuery()
                .eq(MedicalRecord::getPatientId, a.getPatientId())
                .orderByDesc(MedicalRecord::getCreatedAt));
        m.put("history", history);
        return m;
    }

    private SysUser safe(SysUser u) { u.setPassword(null); return u; }
}
