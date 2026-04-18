package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String orderNo;

    private Integer customerId;

    private Integer packageId;

    private BigDecimal totalPrice;

    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date expectedDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String packageName;
}
