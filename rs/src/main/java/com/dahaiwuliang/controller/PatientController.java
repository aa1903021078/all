package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dahaiwuliang.aop.LogOp;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.Notification;
import com.dahaiwuliang.mapper.NotificationMapper;
import com.dahaiwuliang.security.CurrentUser;
import com.dahaiwuliang.service.AppointmentService;
import com.dahaiwuliang.service.MedicalRecordService;
import com.dahaiwuliang.service.ReferralService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {

    private final AppointmentService appointmentService;
    private final MedicalRecordService recordService;
    private final ReferralService referralService;
    private final NotificationMapper notificationMapper;

    @PostMapping("/appointments")
    @LogOp(action = "预约挂号", target = "appointment")
    public R<?> book(@RequestBody BookReq req) {
        return R.ok(appointmentService.book(CurrentUser.id(), req.getScheduleId()));
    }

    @GetMapping("/appointments")
    public R<List<Map<String, Object>>> myAppointments() {
        return R.ok(appointmentService.listByPatient(CurrentUser.id()));
    }

    @PostMapping("/appointments/{id}/cancel")
    @LogOp(action = "取消预约", target = "appointment")
    public R<?> cancel(@PathVariable Long id) {
        appointmentService.cancel(CurrentUser.id(), id);
        return R.ok();
    }

    @GetMapping("/records")
    public R<List<Map<String, Object>>> records() {
        return R.ok(recordService.listByPatient(CurrentUser.id()));
    }

    @GetMapping("/records/by-appointment/{id}")
    public R<Map<String, Object>> recordByAppointment(@PathVariable Long id) {
        return R.ok(recordService.getByAppointment(id, CurrentUser.id(), "PATIENT"));
    }

    @GetMapping("/referrals")
    public R<List<Map<String, Object>>> referrals() {
        return R.ok(referralService.listByPatient(CurrentUser.id()));
    }

    @GetMapping("/notifications")
    public R<List<Notification>> notifications() {
        return R.ok(notificationMapper.selectList(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, CurrentUser.id())
                .orderByDesc(Notification::getCreatedAt)));
    }

    @PostMapping("/notifications/{id}/read")
    public R<?> markRead(@PathVariable Long id) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getUserId().equals(CurrentUser.id())) {
            n.setReadFlag(1);
            notificationMapper.updateById(n);
        }
        return R.ok();
    }

    @Data
    public static class BookReq { private Long scheduleId; }
}
