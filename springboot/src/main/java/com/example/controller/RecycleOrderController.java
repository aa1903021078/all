package com.example.controller;

import com.example.common.Result;
import com.example.entity.RecycleOrder;
import com.example.service.RecycleOrderService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 回收订单前端操作接口
 */
@RestController
@RequestMapping("/recycleOrder")
public class RecycleOrderController {

    @Resource
    private RecycleOrderService recycleOrderService;

    /**
     * 创建订单（用户预约）
     */
    @PostMapping("/create")
    public Result create(@RequestBody RecycleOrder recycleOrder) {
        RecycleOrder newOrder = recycleOrderService.create(recycleOrder);
        return Result.success(newOrder);
    }

    /**
     * 智能派单
     */
    @PostMapping("/dispatch/{orderId}")
    public Result dispatchOrder(@PathVariable Integer orderId) {
        recycleOrderService.dispatchOrder(orderId);
        return Result.success();
    }

    /**
     * 手动派单
     */
    @PostMapping("/manualDispatch")
    public Result manualDispatch(@RequestParam Integer orderId, @RequestParam Integer collectorId) {
        recycleOrderService.manualDispatch(orderId, collectorId);
        return Result.success();
    }

    /**
     * 回收员抢单
     */
    @PostMapping("/grab")
    public Result grabOrder(@RequestParam Integer orderId, @RequestParam Integer collectorId) {
        recycleOrderService.grabOrder(orderId, collectorId);
        return Result.success();
    }

    /**
     * 根据状态查询订单（用于抢单大厅）
     */
    @GetMapping("/selectByStatus/{status}")
    public Result selectByStatus(@PathVariable Integer status) {
        List<RecycleOrder> list = recycleOrderService.selectByStatus(status);
        return Result.success(list);
    }

    /**
     * 回收员接单
     */
    @PostMapping("/accept")
    public Result acceptOrder(@RequestParam Integer orderId, @RequestParam Integer collectorId) {
        recycleOrderService.acceptOrder(orderId, collectorId);
        return Result.success();
    }

    /**
     * 回收员到达
     */
    @PostMapping("/arrive")
    public Result arrive(@RequestParam Integer orderId) {
        recycleOrderService.arrive(orderId);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PostMapping("/complete")
    public Result complete(@RequestBody RecycleOrder recycleOrder) {
        recycleOrderService.complete(recycleOrder);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PostMapping("/cancel")
    public Result cancel(@RequestParam Integer orderId, @RequestParam String reason) {
        recycleOrderService.cancel(orderId, reason);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        recycleOrderService.deleteById(id);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        RecycleOrder recycleOrder = recycleOrderService.selectById(id);
        return Result.success(recycleOrder);
    }

    /**
     * 查询用户的所有订单
     */
    @GetMapping("/selectByUser/{userId}")
    public Result selectByUser(@PathVariable Integer userId) {
        List<RecycleOrder> list = recycleOrderService.selectByUser(userId);
        return Result.success(list);
    }

    /**
     * 查询回收员的所有订单
     */
    @GetMapping("/selectByCollector/{collectorId}")
    public Result selectByCollector(@PathVariable Integer collectorId) {
        List<RecycleOrder> list = recycleOrderService.selectByCollector(collectorId);
        return Result.success(list);
    }

    /**
     * 查询所有（带条件）
     */
    @GetMapping("/selectAll")
    public Result selectAll(RecycleOrder recycleOrder) {
        List<RecycleOrder> list = recycleOrderService.selectAll(recycleOrder);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result SselectPage(RecycleOrder recycleOrder,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<RecycleOrder> pageInfo = recycleOrderService.selectPage(recycleOrder, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 查询待派单订单
     */
    @GetMapping("/selectPendingDispatch")
    public Result selectPendingDispatch() {
        List<RecycleOrder> list = recycleOrderService.selectPendingDispatch();
        return Result.success(list);
    }

    /**
     * 用户评分
     */
    @PostMapping("/rate")
    public Result rateOrder(@RequestParam Integer orderId,
                            @RequestParam Integer rating,
                            @RequestParam String comment) {
        recycleOrderService.rateOrder(orderId, rating, comment);
        return Result.success();
    }

    /**
     * 获取订单统计数据
     */
    @GetMapping("/statistics")
    public Result getStatistics() {
        Map<String, Object> stats = recycleOrderService.getStatistics();
        return Result.success(stats);
    }
}