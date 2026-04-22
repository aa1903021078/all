package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.Item;
import com.yuequge.entity.OrderInfo;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.ItemMapper;
import com.yuequge.mapper.OrderInfoMapper;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 订单接口（第 1 期：下单入库 + 列表；支付宝全链路留到第 2 期）。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderInfoMapper orderMapper;
    private final ItemMapper itemMapper;

    @PostMapping
    public Result<OrderInfo> create(@RequestBody Map<String, Object> body) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        Long itemId = ((Number) body.get("itemId")).longValue();
        Item item = itemMapper.selectById(itemId);
        if (item == null) throw new BizException(404, "商品不存在");

        OrderInfo o = new OrderInfo();
        o.setOrderId(UUID.randomUUID().toString());
        o.setUserId(u.userId());
        o.setItemId(itemId);
        o.setAmount(item.getPrice());
        o.setStatus("0");
        o.setCreateTime(LocalDateTime.now());
        orderMapper.insert(o);
        return Result.ok(o);
    }

    @GetMapping("/my")
    public Result<PageResult<OrderInfo>> my(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        Page<OrderInfo> p = orderMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getUserId, u.userId())
                        .orderByDesc(OrderInfo::getCreateTime));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }
}

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
class AdminOrderController {

    private final OrderInfoMapper orderMapper;

    @GetMapping
    public Result<PageResult<OrderInfo>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size) {
        Page<OrderInfo> p = orderMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<OrderInfo>().orderByDesc(OrderInfo::getCreateTime));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @PutMapping("/{orderId}/status")
    public Result<Void> setStatus(@PathVariable String orderId, @RequestParam String status) {
        OrderInfo o = orderMapper.selectById(orderId);
        if (o == null) throw new BizException(404, "订单不存在");
        o.setStatus(status);
        orderMapper.updateById(o);
        return Result.ok();
    }
}
