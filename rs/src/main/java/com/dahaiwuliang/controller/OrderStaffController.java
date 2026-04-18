package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.OrderStaff;
import com.dahaiwuliang.entity.Orders;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.OrderStaffService;
import com.dahaiwuliang.service.OrdersService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orderStaff")
public class OrderStaffController {

    @Autowired
    private OrderStaffService orderStaffService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer orderId) {
        Page<OrderStaff> page = orderStaffService.list(pageNum, pageSize, orderId);
        for (OrderStaff os : page.getRecords()) {
            fillOrderStaffNames(os);
        }
        return Result.ok(page);
    }

    @PostMapping("/add")
    public Result add(@RequestBody OrderStaff orderStaff) {
        orderStaffService.save(orderStaff);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        orderStaffService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/byOrder/{orderId}")
    public Result byOrder(@PathVariable Integer orderId) {
        List<OrderStaff> list = orderStaffService.findByOrderId(orderId);
        for (OrderStaff os : list) {
            fillOrderStaffNames(os);
        }
        return Result.ok(list);
    }

    private void fillOrderStaffNames(OrderStaff os) {
        if (os.getStaffId() != null) {
            User staff = userService.getById(os.getStaffId());
            if (staff != null) {
                os.setStaffName(staff.getRealName());
            }
        }
        if (os.getOrderId() != null) {
            Orders order = ordersService.getById(os.getOrderId());
            if (order != null) {
                os.setOrderNo(order.getOrderNo());
            }
        }
    }
}
