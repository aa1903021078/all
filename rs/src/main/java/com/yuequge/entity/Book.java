package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String type;
    private String name;
    private String description;
    private String coverUrl;
    /** 热度（阅读/收藏次数累积） */
    private Long heat;
    /** 评分 0-10 */
    private BigDecimal rating;
}
