package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.Orders;
import com.dahaiwuliang.entity.StaffChangeRequest;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.OrdersService;
import com.dahaiwuliang.service.StaffChangeRequestService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staffChange")
public class StaffChangeRequestController {

    @Autowired
    private StaffChangeRequestService staffChangeRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam(required = false) Integer status) {
        Page<StaffChangeRequest> page = staffChangeRequestService.list(pageNum, pageSize, customerId, status);
        for (StaffChangeRequest req : page.getRecords()) {
            fillNames(req);
        }
        return Result.ok(page);
    }

    @PostMapping("/add")
    public Result add(@RequestBody StaffChangeRequest request) {
        if (request.getStatus() == null) {
            request.setStatus(0);
        }
        staffChangeRequestService.save(request);
        return Result.ok();
    }

    @PutMapping("/handle")
    public Result handle(@RequestBody StaffChangeRequest request) {
        StaffChangeRequest update = new StaffChangeRequest();
        update.setId(request.getId());
        update.setStatus(request.getStatus());
        update.setReply(request.getReply());
        staffChangeRequestService.updateById(update);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        staffChangeRequestService.removeById(id);
        return Result.ok();
    }

    private void fillNames(StaffChangeRequest req) {
        if (req.getCustomerId() != null) {
            User customer = userService.getById(req.getCustomerId());
            if (customer != null) {
                req.setCustomerName(customer.getRealName());
            }
        }
        if (req.getOldStaffId() != null) {
            User oldStaff = userService.getById(req.getOldStaffId());
            if (oldStaff != null) {
                req.setOldStaffName(oldStaff.getRealName());
            }
        }
        if (req.getNewStaffId() != null) {
            User newStaff = userService.getById(req.getNewStaffId());
            if (newStaff != null) {
                req.setNewStaffName(newStaff.getRealName());
            }
        }
        if (req.getOrderId() != null) {
            Orders order = ordersService.getById(req.getOrderId());
            if (order != null) {
                req.setOrderNo(order.getOrderNo());
            }
        }
    }
}
