package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("`package`")
public class Package {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private String description;

    private BigDecimal price;

    private Integer duration;

    private String services;

    private Integer status;

    private String image;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
