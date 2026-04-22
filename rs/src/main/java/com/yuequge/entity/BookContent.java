package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("book_content")
public class BookContent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bookId;
    private Integer chapterNumber;
    private String chapterTitle;
    private String content;
}
