package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("staff_change_request")
public class StaffChangeRequest {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer customerId;

    private Integer oldStaffId;

    private Integer newStaffId;

    private String reason;

    private Integer status;

    private String reply;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String oldStaffName;

    @TableField(exist = false)
    private String newStaffName;

    @TableField(exist = false)
    private String orderNo;
}
