package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("schedule")
public class Schedule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long doctorId;
    private Long deptId;
    private Long hospitalId;
    private LocalDate workDate;
    private String timeSlot;        // AM / PM
    private Integer totalQuota;
    private Integer remainingQuota;
    private String status;          // OPEN / CLOSED
    @Version
    private Integer version;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}
