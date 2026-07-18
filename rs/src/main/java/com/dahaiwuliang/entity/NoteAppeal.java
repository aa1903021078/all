package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商家对探店笔记(差评)的申诉
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("note_appeal")
public class NoteAppeal extends BaseEntity {

    private Long noteId;
    private Long shopId;
    private Long merchantId;
    /** 申诉理由 */
    private String reason;
    /** 0待处理 1已受理 2已驳回 */
    private Integer status;
    /** 平台处理回复 */
    private String reply;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String noteTitle;
    @TableField(exist = false)
    private String noteContent;
    @TableField(exist = false)
    private Integer noteRating;
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private String merchantName;
}
