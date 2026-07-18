package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 预约
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation")
public class Reservation extends BaseEntity {

    private Long userId;
    private Long shopId;
    private LocalDateTime reserveTime;
    private Integer peopleCount;
    private String contactName;
    private String contactPhone;
    private String remark;
    /** 0待确认 1已确认 2已拒绝 3已完成 4已取消 */
    private Integer status;

    // ---- 展示字段(不入库) ----
    @TableField(exist = false)
    private String shopName;
    @TableField(exist = false)
    private String shopCover;
    @TableField(exist = false)
    private String userName;
}
