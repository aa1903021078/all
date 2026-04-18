package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("complaint")
public class Complaint {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer customerId;

    private String content;

    private String reply;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date replyTime;

    @TableField(exist = false)
    private String customerName;

    @TableField(exist = false)
    private String orderNo;
}
