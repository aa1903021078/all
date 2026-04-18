package com.example.controller;


import com.example.common.Result;
import com.example.entity.PointsExchange;
import com.example.service.PointsExchangeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分兑换前端操作接口
 */
@RestController
@RequestMapping("/pointsExchange")
public class PointsExchangeController {

    @Resource
    private PointsExchangeService pointsExchangeService;

    /**
     * 兑换商品
     */
    @PostMapping("/exchange")
    public Result exchange(
            @RequestParam Integer userId,
            @RequestParam Integer goodsId) {
        pointsExchangeService.exchange(userId, goodsId);
        return Result.success();
    }

    /**
     * 查询用户的兑换记录
     */
    @GetMapping("/selectByUser/{userId}")
    public Result selectByUser(@PathVariable Integer userId) {
        List<PointsExchange> list = pointsExchangeService.selectByUser(userId);
        return Result.success(list);
    }

    /**
     * 分页查询（管理员用）
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) Integer status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<PointsExchange> pageInfo = pointsExchangeService.selectPage(status, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 发货（管理员）
     */
    @PutMapping("/ship/{id}")
    public Result ship(@PathVariable Integer id) {
        pointsExchangeService.updateStatus(id, 1);
        return Result.success();
    }

    /**
     * 取消兑换
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Integer id) {
        pointsExchangeService.cancel(id);
        return Result.success();
    }

}
