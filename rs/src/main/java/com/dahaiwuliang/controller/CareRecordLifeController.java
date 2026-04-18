package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.CareRecordLife;
import com.dahaiwuliang.entity.OrderInfant;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.CareRecordLifeService;
import com.dahaiwuliang.service.OrderInfantService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/careLife")
public class CareRecordLifeController {

    @Autowired
    private CareRecordLifeService careRecordLifeService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderInfantService orderInfantService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer orderId,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam(required = false) Integer staffId,
                       @RequestParam(required = false) Integer infantId) {
        Page<CareRecordLife> page = careRecordLifeService.list(pageNum, pageSize, orderId, customerId, staffId, infantId);
        for (CareRecordLife record : page.getRecords()) {
            fillNames(record);
        }
        return Result.ok(page);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        CareRecordLife record = careRecordLifeService.getById(id);
        if (record == null) {
            return Result.error("记录不存在");
        }
        fillNames(record);
        return Result.ok(record);
    }

    @PostMapping("/add")
    public Result add(@RequestBody CareRecordLife record) {
        careRecordLifeService.save(record);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result update(@RequestBody CareRecordLife record) {
        careRecordLifeService.updateById(record);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        careRecordLifeService.removeById(id);
        return Result.ok();
    }

    private void fillNames(CareRecordLife record) {
        if (record.getStaffId() != null) {
            User staff = userService.getById(record.getStaffId());
            if (staff != null) {
                record.setStaffName(staff.getRealName());
            }
        }
        if (record.getCustomerId() != null) {
            User customer = userService.getById(record.getCustomerId());
            if (customer != null) {
                record.setCustomerName(customer.getRealName());
            }
        }
        if (record.getInfantId() != null) {
            OrderInfant infant = orderInfantService.getById(record.getInfantId());
            if (infant != null) {
                record.setInfantName(infant.getName());
            }
        }
    }
}
