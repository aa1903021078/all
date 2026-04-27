package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.aop.LogOp;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.MedicalRecord;
import com.dahaiwuliang.entity.Referral;
import com.dahaiwuliang.entity.Schedule;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.SysUserMapper;
import com.dahaiwuliang.security.CurrentUser;
import com.dahaiwuliang.service.AppointmentService;
import com.dahaiwuliang.service.MedicalRecordService;
import com.dahaiwuliang.service.ReferralService;
import com.dahaiwuliang.service.ScheduleService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService recordService;
    private final ReferralService referralService;
    private final ScheduleService scheduleService;
    private final SysUserMapper userMapper;

    @GetMapping("/appointments")
    public R<List<Map<String, Object>>> appointments(@RequestParam(required = false) String status) {
        return R.ok(appointmentService.listByDoctor(CurrentUser.id(), status));
    }

    @GetMapping("/records/by-appointment/{id}")
    public R<Map<String, Object>> recordByAppointment(@PathVariable Long id) {
        return R.ok(recordService.getByAppointment(id, CurrentUser.id(), "DOCTOR"));
    }

    @PostMapping("/diagnose/{appointmentId}")
    @LogOp(action = "完成接诊", target = "medical_record")
    public R<MedicalRecord> diagnose(@PathVariable Long appointmentId, @RequestBody MedicalRecord r) {
        return R.ok(recordService.diagnose(CurrentUser.id(), appointmentId, r));
    }

    @GetMapping("/schedules")
    public R<List<Map<String, Object>>> schedules() {
        return R.ok(scheduleService.listByDoctor(CurrentUser.id()));
    }

    @PostMapping("/schedules")
    @LogOp(action = "新增排班", target = "schedule")
    public R<Schedule> createSchedule(@RequestBody Schedule s) {
        s.setDoctorId(CurrentUser.id());
        return R.ok(scheduleService.create(s));
    }

    @PostMapping("/schedules/{id}/close")
    @LogOp(action = "停诊", target = "schedule")
    public R<?> closeSchedule(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        scheduleService.close(id, body == null ? null : body.get("reason"));
        return R.ok();
    }

    // referrals: outgoing & incoming
    @GetMapping("/referrals/outgoing")
    public R<List<Map<String, Object>>> outgoing() {
        return R.ok(referralService.listOutgoing(CurrentUser.id()));
    }

    @GetMapping("/referrals/incoming")
    public R<List<Map<String, Object>>> incoming() {
        SysUser me = userMapper.selectById(CurrentUser.id());
        if (me == null || me.getHospitalId() == null) return R.ok(java.util.Collections.emptyList());
        return R.ok(referralService.listIncoming(me.getHospitalId()));
    }

    @PostMapping("/referrals")
    @LogOp(action = "发起转诊", target = "referral")
    public R<Referral> createReferral(@RequestBody Referral input) {
        return R.ok(referralService.create(CurrentUser.id(), input));
    }

    @PostMapping("/referrals/{id}/approve")
    @LogOp(action = "审核通过转诊", target = "referral")
    public R<Referral> approve(@PathVariable Long id, @RequestBody ApproveReq req) {
        return R.ok(referralService.approve(CurrentUser.id(), id, req.getTargetScheduleId()));
    }

    @PostMapping("/referrals/{id}/reject")
    @LogOp(action = "驳回转诊", target = "referral")
    public R<Referral> reject(@PathVariable Long id, @RequestBody RejectReq req) {
        return R.ok(referralService.reject(CurrentUser.id(), id, req.getReason()));
    }

    @GetMapping("/colleagues")
    public R<List<SysUser>> colleagues() {
        SysUser me = userMapper.selectById(CurrentUser.id());
        if (me == null) return R.ok(java.util.Collections.emptyList());
        List<SysUser> doctors = userMapper.selectList(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getRole, "DOCTOR"));
        doctors.forEach(u -> u.setPassword(null));
        return R.ok(doctors);
    }

    @Data public static class ApproveReq { private Long targetScheduleId; }
    @Data public static class RejectReq { private String reason; }
}
