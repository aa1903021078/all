package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("department")
public class Department {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long hospitalId;
    private String name;
    private String description;
    @TableLogic
    private Integer deleted;
}
