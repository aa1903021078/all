package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("book")
public class Book {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private String grade;
    private String major;
    private String courseName;
    private Long categoryId;
    private String conditionDesc;
    private Integer conditionLevel;
    private String coverImg;
    private String detailImgs;
    private Integer status;
    private String wantBookDesc;
    private String acceptCategory;
    private Integer viewCount;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
