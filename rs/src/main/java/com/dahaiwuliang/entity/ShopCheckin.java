package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 点亮店铺
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("shop_checkin")
public class ShopCheckin extends CreateEntity {

    private Long userId;
    private Long shopId;
    private Long noteId;
    private LocalDateTime checkinTime;
}
