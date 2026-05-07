package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("exchange_order")
public class ExchangeOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long requesterId;
    private Long requesterBookId;
    private Long ownerId;
    private Long ownerBookId;
    private Integer status;
    private String exchangeMethod;
    private String exchangeLocation;
    private Date exchangeTime;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
