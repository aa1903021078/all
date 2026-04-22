package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String author;
    /** 0=草稿 1=已发布 2=下架 */
    private Integer status;
    private Integer priority;
    private String coverUrl;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
