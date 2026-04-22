package com.yuequge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_info")
public class OrderInfo {
    @TableId(value = "order_id", type = IdType.INPUT)
    private String orderId;
    private Long userId;
    private Long itemId;
    /** '0'=未支付 '1'=已支付 '2'=已关闭 */
    private String status;
    private LocalDateTime createTime;
    private BigDecimal amount;
}
