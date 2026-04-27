package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("hospital")
public class Hospital {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String level;
    private Long parentId;
    private String address;
    private String phone;
    @TableLogic
    private Integer deleted;
}
