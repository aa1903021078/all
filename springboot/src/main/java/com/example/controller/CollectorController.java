package com.example.controller;

import com.example.common.Result;
import com.example.dto.CollectorRegisterDTO;
import com.example.entity.Collector;
import com.example.entity.User;
import com.example.service.CollectorService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 回收员前端操作接口
 */
@RestController
@RequestMapping("/collector")
public class CollectorController {

    @Resource
    private CollectorService collectorService;

    /**
     * 管理员新增回收员（直接通过）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Collector collector) {
        collectorService.add(collector);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        collectorService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Collector collector) {
        collectorService.updateById(collector);
        return Result.success();
    }

    /**
     * 回收员自主注册（待审核）
     */
    @PostMapping("/register")
    public Result register(@RequestBody CollectorRegisterDTO collectorRegisterDTO) {
        collectorService.register(collectorRegisterDTO);
        return Result.success("注册成功，请等待管理员审核");
    }

    /**
     * 查询待审核列表（管理员用）
     */
    @GetMapping("/pendingAudit")
    public Result selectPendingAudit() {
        List<Collector> list = collectorService.selectPendingAudit();
        return Result.success(list);
    }

    /**
     * 审核通过（管理员用）
     */
    @PutMapping("/auditPass/{id}")
    public Result auditPass(@PathVariable Integer id) {
        collectorService.auditPass(id);
        return Result.success("审核通过");
    }

    /**
     * 审核拒绝（管理员用）
     */
    @PutMapping("/auditReject/{id}")
    public Result auditReject(@PathVariable Integer id, @RequestParam String reason) {
        collectorService.auditReject(id, reason);
        return Result.success("已拒绝申请");
    }

    /**
     * 查询审核状态（新增）
     */
    @GetMapping("/queryStatus")
    public Result queryStatus(@RequestParam String phone, @RequestParam String idCard) {
        Collector collector = collectorService.queryStatus(phone, idCard);
        return Result.success(collector);
    }

    /**
     * 重新提交申请（新增）
     */
    @PutMapping("/resubmit/{id}")
    public Result resubmit(@PathVariable Integer id, @RequestBody CollectorRegisterDTO dto) {
        collectorService.resubmit(id, dto);
        return Result.success("重新提交成功，请等待审核");
    }

    /**
     * 更新工作状态
     */
    @PutMapping("/updateWorkStatus")
    public Result updateWorkStatus(@RequestParam Integer id, @RequestParam Integer workStatus) {
        collectorService.updateWorkStatus(id, workStatus);
        return Result.success();
    }

    /**
     * 更新位置信息
     */
    @PutMapping("/updateLocation")
    public Result updateLocation(@RequestParam Integer id,
                                 @RequestParam Double lat,
                                 @RequestParam Double lng) {
        collectorService.updateLocation(id, lat, lng);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Collector collector = collectorService.selectById(id);
        return Result.success(collector);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Collector collector) {
        List<Collector> list = collectorService.selectAll(collector);
        return Result.success(list);
    }

    /**
     * @param pageNum 当前页码
     * @param pageSize 每页展示的个数
     * @return 分页数据
     * 接口的请求方式：http://localhost:9090/user/selectPage?pageNum=1&pageSize=10
     */
    @GetMapping("selectPage")
    public Result selectPage(Collector collector, @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name) {
        PageInfo<Collector> pageInfo = collectorService.selectPage(collector,pageNum, pageSize,name);
        return Result.success(pageInfo);
    }

    /**
     * 查询附近可用的回收员
     */
    @GetMapping("/selectNearby")
    public Result selectNearby(@RequestParam Double lat,
                               @RequestParam Double lng,
                               @RequestParam(defaultValue = "5000") Double radius) {
        List<Collector> list = collectorService.selectNearby(lat, lng, radius);
        return Result.success(list);
    }

    /**
     * 查询回收员今日订单统计
     */
    @GetMapping("/todayStats/{id}")
    public Result getTodayStats(@PathVariable Integer id) {
        return Result.success(collectorService.getTodayStats(id));
    }
}
