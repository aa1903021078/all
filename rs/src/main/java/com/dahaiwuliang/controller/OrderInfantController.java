package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.OrderInfant;
import com.dahaiwuliang.service.OrderInfantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orderInfant")
public class OrderInfantController {

    @Autowired
    private OrderInfantService orderInfantService;

    @GetMapping("/byOrder/{orderId}")
    public Result byOrder(@PathVariable Integer orderId) {
        List<OrderInfant> list = orderInfantService.findByOrderId(orderId);
        return Result.ok(list);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        OrderInfant infant = orderInfantService.getById(id);
        if (infant == null) {
            return Result.error("婴儿信息不存在");
        }
        return Result.ok(infant);
    }

    @PostMapping("/add")
    public Result add(@RequestBody OrderInfant infant) {
        orderInfantService.save(infant);
        return Result.ok(infant);
    }

    @PutMapping("/update")
    public Result update(@RequestBody OrderInfant infant) {
        orderInfantService.updateById(infant);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        orderInfantService.removeById(id);
        return Result.ok();
    }
}
