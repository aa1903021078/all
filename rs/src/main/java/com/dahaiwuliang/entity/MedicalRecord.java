package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("medical_record")
public class MedicalRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long hospitalId;
    private Long deptId;
    private String chiefComplaint;
    private String diagnosis;
    private String prescription;
    private String advice;
    private String imageUrl;
    private LocalDateTime createdAt;
}
