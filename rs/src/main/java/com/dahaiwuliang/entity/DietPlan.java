package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("diet_plan")
public class DietPlan {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private Integer staffId;

    private Integer customerId;

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date planDate;

    private String breakfast;

    private String lunch;

    private String dinner;

    private String snack;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @TableField(exist = false)
    private String staffName;

    @TableField(exist = false)
    private String customerName;
}
