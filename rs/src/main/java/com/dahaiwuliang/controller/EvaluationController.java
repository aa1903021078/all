package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.Evaluation;
import com.dahaiwuliang.entity.Orders;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.service.EvaluationService;
import com.dahaiwuliang.service.OrdersService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrdersService ordersService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer orderId,
                       @RequestParam(required = false) Integer customerId) {
        Page<Evaluation> page = evaluationService.list(pageNum, pageSize, orderId, customerId);
        for (Evaluation eval : page.getRecords()) {
            fillNames(eval);
        }
        return Result.ok(page);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Evaluation evaluation) {
        evaluationService.save(evaluation);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        evaluationService.removeById(id);
        return Result.ok();
    }

    private void fillNames(Evaluation eval) {
        if (eval.getCustomerId() != null) {
            User customer = userService.getById(eval.getCustomerId());
            if (customer != null) {
                eval.setCustomerName(customer.getRealName());
            }
        }
        if (eval.getOrderId() != null) {
            Orders order = ordersService.getById(eval.getOrderId());
            if (order != null) {
                eval.setOrderNo(order.getOrderNo());
            }
        }
    }
}
