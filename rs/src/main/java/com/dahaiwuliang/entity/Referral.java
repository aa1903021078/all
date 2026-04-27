package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("referral")
public class Referral {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long patientId;
    private Long fromHospitalId;
    private Long fromDeptId;
    private Long fromDoctorId;
    private Long toHospitalId;
    private Long toDeptId;
    private Long toDoctorId;
    private String type;            // UP / DOWN
    private String summary;
    private String rehabPlan;
    private String imageUrl;
    private String status;          // DRAFT / REVIEWING / ACCEPTED / VISITING / COMPLETED / REJECTED
    private String rejectReason;
    private Long targetScheduleId;
    private Long targetAppointmentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
