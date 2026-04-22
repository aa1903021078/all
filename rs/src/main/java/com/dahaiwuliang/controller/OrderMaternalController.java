package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.OrderMaternal;
import com.dahaiwuliang.service.OrderMaternalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orderMaternal")
public class OrderMaternalController {

    @Autowired
    private OrderMaternalService orderMaternalService;

    @GetMapping("/byOrder/{orderId}")
    public Result byOrder(@PathVariable Integer orderId) {
        OrderMaternal maternal = orderMaternalService.findByOrderId(orderId);
        return Result.ok(maternal);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        OrderMaternal maternal = orderMaternalService.getById(id);
        if (maternal == null) {
            return Result.error("产妇信息不存在");
        }
        return Result.ok(maternal);
    }

    @PostMapping("/add")
    public Result add(@RequestBody OrderMaternal maternal) {
        orderMaternalService.save(maternal);
        return Result.ok(maternal);
    }

    @PutMapping("/update")
    public Result update(@RequestBody OrderMaternal maternal) {
        orderMaternalService.updateById(maternal);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        orderMaternalService.removeById(id);
        return Result.ok();
    }
}
