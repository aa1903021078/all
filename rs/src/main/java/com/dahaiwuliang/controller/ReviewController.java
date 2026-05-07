package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.ExchangeOrder;
import com.dahaiwuliang.entity.Review;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.ExchangeOrderService;
import com.dahaiwuliang.service.ReviewService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private UserService userService;

    @PostMapping("/submit")
    public Result<?> submit(HttpServletRequest request, @RequestBody Review review) {
        Long userId = (Long) request.getAttribute("userId");
        ExchangeOrder order = exchangeOrderService.getById(review.getOrderId());
        if (order == null || order.getStatus() != 4) {
            return Result.error("订单未完成，无法评价");
        }
        // 确定被评价人
        if (order.getRequesterId().equals(userId)) {
            review.setToUserId(order.getOwnerId());
        } else if (order.getOwnerId().equals(userId)) {
            review.setToUserId(order.getRequesterId());
        } else {
            return Result.error("无权评价");
        }
        review.setFromUserId(userId);
        reviewService.save(review);

        // 更新被评价人信用分
        User toUser = userService.getById(review.getToUserId());
        int scoreDelta = review.getCreditScore() - 3; // 3分为基准，高于加分，低于减分
        toUser.setCreditScore(Math.max(0, Math.min(100, toUser.getCreditScore() + scoreDelta)));
        userService.updateById(toUser);

        return Result.success();
    }

    @GetMapping("/list/{userId}")
    public Result<?> list(@PathVariable Long userId) {
        List<Review> reviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getToUserId, userId).orderByDesc(Review::getCreateTime));
        return Result.success(reviews);
    }
}
