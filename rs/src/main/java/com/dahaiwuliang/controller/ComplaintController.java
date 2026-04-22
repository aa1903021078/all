package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.Complaint;
import com.dahaiwuliang.entity.Orders;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.ComplaintService;
import com.dahaiwuliang.service.OrdersService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/api/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam(required = false) Integer status) {
        Page<Complaint> page = complaintService.list(pageNum, pageSize, customerId, status);
        for (Complaint complaint : page.getRecords()) {
            fillNames(complaint);
        }
        return Result.ok(page);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Complaint complaint) {
        if (complaint.getStatus() == null) {
            complaint.setStatus(0);
        }
        complaintService.save(complaint);
        return Result.ok();
    }

    @PutMapping("/reply")
    public Result reply(@RequestBody Complaint complaint) {
        Complaint update = new Complaint();
        update.setId(complaint.getId());
        update.setReply(complaint.getReply());
        update.setStatus(1);
        update.setReplyTime(new Date());
        complaintService.updateById(update);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        complaintService.removeById(id);
        return Result.ok();
    }

    private void fillNames(Complaint complaint) {
        if (complaint.getCustomerId() != null) {
            User customer = userService.getById(complaint.getCustomerId());
            if (customer != null) {
                complaint.setCustomerName(customer.getRealName());
            }
        }
        if (complaint.getOrderId() != null) {
            Orders order = ordersService.getById(complaint.getOrderId());
            if (order != null) {
                complaint.setOrderNo(order.getOrderNo());
            }
        }
    }
}
