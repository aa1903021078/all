package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.DietPlan;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.DietPlanService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dietPlan")
public class DietPlanController {

    @Autowired
    private DietPlanService dietPlanService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer orderId,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam(required = false) Integer staffId) {
        Page<DietPlan> page = dietPlanService.list(pageNum, pageSize, orderId, customerId, staffId);
        for (DietPlan plan : page.getRecords()) {
            fillNames(plan);
        }
        return Result.ok(page);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        DietPlan plan = dietPlanService.getById(id);
        if (plan == null) {
            return Result.error("记录不存在");
        }
        fillNames(plan);
        return Result.ok(plan);
    }

    @PostMapping("/add")
    public Result add(@RequestBody DietPlan plan) {
        dietPlanService.save(plan);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result update(@RequestBody DietPlan plan) {
        dietPlanService.updateById(plan);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        dietPlanService.removeById(id);
        return Result.ok();
    }

    private void fillNames(DietPlan plan) {
        if (plan.getStaffId() != null) {
            User staff = userService.getById(plan.getStaffId());
            if (staff != null) {
                plan.setStaffName(staff.getRealName());
            }
        }
        if (plan.getCustomerId() != null) {
            User customer = userService.getById(plan.getCustomerId());
            if (customer != null) {
                plan.setCustomerName(customer.getRealName());
            }
        }
    }
}
