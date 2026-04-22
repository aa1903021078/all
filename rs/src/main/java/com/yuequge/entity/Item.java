package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_item")
public class Item {
    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;
    private String itemName;
    private String stock;
    private String photo;
    private BigDecimal price;
    private LocalDateTime createTime;
}
