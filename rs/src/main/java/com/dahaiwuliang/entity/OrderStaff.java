package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("order_staff")
public class OrderStaff {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer staffId;

    private Integer staffRole;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private String staffName;

    @TableField(exist = false)
    private String orderNo;
}
