package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.Orders;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.OrdersService;
import com.dahaiwuliang.service.PackageService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private UserService userService;

    @Autowired
    private PackageService packageService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam(required = false) String orderNo) {
        Page<Orders> page = ordersService.list(pageNum, pageSize, status, customerId, orderNo);
        for (Orders order : page.getRecords()) {
            fillOrderNames(order);
        }
        return Result.ok(page);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Orders order = ordersService.getById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        fillOrderNames(order);
        return Result.ok(order);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        orders.setOrderNo(ordersService.generateOrderNo());
        if (orders.getStatus() == null) {
            orders.setStatus(0);
        }
        ordersService.save(orders);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.ok();
    }

    @PutMapping("/status")
    public Result updateStatus(@RequestBody Orders orders) {
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setStatus(orders.getStatus());
        ordersService.updateById(update);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        ordersService.removeById(id);
        return Result.ok();
    }

    @GetMapping("/my/{customerId}")
    public Result myOrders(@PathVariable Integer customerId) {
        List<Orders> list = ordersService.findByCustomerId(customerId);
        for (Orders order : list) {
            fillOrderNames(order);
        }
        return Result.ok(list);
    }

    private void fillOrderNames(Orders order) {
        if (order.getCustomerId() != null) {
            User customer = userService.getById(order.getCustomerId());
            if (customer != null) {
                order.setCustomerName(customer.getRealName());
            }
        }
        if (order.getPackageId() != null) {
            com.dahaiwuliang.entity.Package pkg = packageService.getById(order.getPackageId());
            if (pkg != null) {
                order.setPackageName(pkg.getName());
            }
        }
    }
}
