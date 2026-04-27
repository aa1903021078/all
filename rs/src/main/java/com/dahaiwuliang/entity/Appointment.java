package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long scheduleId;
    private Long doctorId;
    private Long hospitalId;
    private Long deptId;
    private String status;     // BOOKED / VISITED / CANCELLED
    private String source;     // SELF / REFERRAL
    private Long referralId;
    private LocalDateTime createdAt;
}
