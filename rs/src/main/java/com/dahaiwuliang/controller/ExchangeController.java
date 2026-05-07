package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.Book;
import com.dahaiwuliang.entity.ExchangeOrder;
import com.dahaiwuliang.entity.SystemNotification;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.mapper.SystemNotificationMapper;
import com.dahaiwuliang.service.BookService;
import com.dahaiwuliang.service.ExchangeOrderService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeController {

    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;
    @Autowired
    private SystemNotificationMapper notificationMapper;

    @PostMapping("/apply")
    public Result<?> apply(HttpServletRequest request, @RequestBody ExchangeOrder order) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user.getStatus() != 1) {
            return Result.error("请先完成校园认证");
        }
        order.setRequesterId(userId);
        Book ownerBook = bookService.getById(order.getOwnerBookId());
        if (ownerBook == null || ownerBook.getStatus() != 1) {
            return Result.error("该图书不可置换");
        }
        order.setOwnerId(ownerBook.getUserId());
        order.setStatus(0);
        exchangeOrderService.save(order);

        // 发送通知给图书持有人
        SystemNotification notification = new SystemNotification();
        notification.setUserId(ownerBook.getUserId());
        notification.setTitle("收到置换申请");
        notification.setContent("有人想要置换您的《" + ownerBook.getTitle() + "》");
        notification.setType(1);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        return Result.success(order);
    }

    @PutMapping("/accept/{id}")
    public Result<?> accept(HttpServletRequest request, @PathVariable Long id, @RequestBody(required = false) ExchangeOrder params) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeOrder order = exchangeOrderService.getById(id);
        if (order == null || !order.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        if (order.getStatus() != 0) {
            return Result.error("订单状态不正确");
        }
        order.setStatus(1);
        if (params != null) {
            order.setExchangeMethod(params.getExchangeMethod());
            order.setExchangeLocation(params.getExchangeLocation());
            order.setExchangeTime(params.getExchangeTime());
        }
        exchangeOrderService.updateById(order);

        // 通知申请人
        SystemNotification notification = new SystemNotification();
        notification.setUserId(order.getRequesterId());
        notification.setTitle("置换申请已被接受");
        notification.setContent("您的置换申请已被对方接受，请尽快协商交换时间和地点");
        notification.setType(1);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        return Result.success();
    }

    @PutMapping("/reject/{id}")
    public Result<?> reject(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeOrder order = exchangeOrderService.getById(id);
        if (order == null || !order.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        order.setStatus(2);
        exchangeOrderService.updateById(order);

        SystemNotification notification = new SystemNotification();
        notification.setUserId(order.getRequesterId());
        notification.setTitle("置换申请被拒绝");
        notification.setContent("很遗憾，您的置换申请被对方拒绝");
        notification.setType(1);
        notification.setIsRead(0);
        notificationMapper.insert(notification);

        return Result.success();
    }

    @PutMapping("/confirm/{id}")
    public Result<?> confirm(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeOrder order = exchangeOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getRequesterId().equals(userId) && !order.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        order.setStatus(4); // 已完成
        exchangeOrderService.updateById(order);

        // 更新图书状态为已置换
        Book requesterBook = bookService.getById(order.getRequesterBookId());
        Book ownerBook = bookService.getById(order.getOwnerBookId());
        if (requesterBook != null) {
            requesterBook.setStatus(3);
            bookService.updateById(requesterBook);
        }
        if (ownerBook != null) {
            ownerBook.setStatus(3);
            bookService.updateById(ownerBook);
        }

        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result<?> cancel(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeOrder order = exchangeOrderService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!order.getRequesterId().equals(userId) && !order.getOwnerId().equals(userId)) {
            return Result.error("无权操作");
        }
        order.setStatus(5);
        exchangeOrderService.updateById(order);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list(HttpServletRequest request, @RequestParam(required = false) Integer status) {
        Long userId = (Long) request.getAttribute("userId");
        LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(ExchangeOrder::getRequesterId, userId).or().eq(ExchangeOrder::getOwnerId, userId));
        if (status != null) {
            wrapper.eq(ExchangeOrder::getStatus, status);
        }
        wrapper.orderByDesc(ExchangeOrder::getCreateTime);
        List<ExchangeOrder> orders = exchangeOrderService.list(wrapper);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        ExchangeOrder order = exchangeOrderService.getById(id);
        return Result.success(order);
    }
}
