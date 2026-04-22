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
import com.yuequge.service.AlipayService;
import com.yuequge.service.MessageQueueService;
import com.yuequge.util.UserContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 订单 + 支付接口（第 2 期：支付宝沙箱接入）。
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderInfoMapper orderMapper;
    private final ItemMapper itemMapper;
    private final AlipayService alipayService;
    private final MessageQueueService mqService;

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

        // 可选：发送订单超时延迟消息（RocketMQ 不可用时自动降级）
        mqService.sendOrderTimeout(o.getOrderId(), 15);
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

    /** 查询订单状态（支付成功回跳时轮询）。 */
    @GetMapping("/{orderId}")
    public Result<OrderInfo> detail(@PathVariable String orderId) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        OrderInfo o = orderMapper.selectById(orderId);
        if (o == null) throw new BizException(404, "订单不存在");
        if (!u.isAdmin() && !o.getUserId().equals(u.userId())) throw new BizException(403, "无权访问");
        return Result.ok(o);
    }

    /**
     * 发起支付宝 PC 支付：返回自动提交的 HTML 表单；前端以新窗口写入执行。
     */
    @GetMapping(value = "/{orderId}/pay", produces = "text/html;charset=UTF-8")
    public void pay(@PathVariable String orderId, HttpServletResponse resp) throws IOException {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        OrderInfo o = orderMapper.selectById(orderId);
        if (o == null) throw new BizException(404, "订单不存在");
        if (!o.getUserId().equals(u.userId())) throw new BizException(403, "无权访问");
        if (!"0".equals(o.getStatus())) throw new BizException("订单当前状态不可支付");

        Item item = itemMapper.selectById(o.getItemId());
        String subject = item != null && item.getItemName() != null ? item.getItemName() : ("阅趣阁订单-" + orderId);
        String html = alipayService.buildPagePayForm(orderId, o.getAmount(), subject);
        resp.setContentType("text/html;charset=UTF-8");
        resp.getOutputStream().write(html.getBytes(StandardCharsets.UTF_8));
        resp.getOutputStream().flush();
    }

    /** 用户主动取消订单。 */
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancel(@PathVariable String orderId) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        OrderInfo o = orderMapper.selectById(orderId);
        if (o == null) throw new BizException(404, "订单不存在");
        if (!o.getUserId().equals(u.userId())) throw new BizException(403, "无权访问");
        if (!"0".equals(o.getStatus())) throw new BizException("仅未支付订单可取消");
        o.setStatus("2");
        orderMapper.updateById(o);
        return Result.ok();
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
