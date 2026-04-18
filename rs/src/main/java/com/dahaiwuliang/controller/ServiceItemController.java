package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.entity.ServiceItem;
import com.dahaiwuliang.service.ServiceItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/service")
public class ServiceItemController {

    @Autowired
    private ServiceItemService serviceItemService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String name) {
        Page<ServiceItem> page = serviceItemService.list(pageNum, pageSize, status, name);
        return Result.ok(page);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        ServiceItem item = serviceItemService.getById(id);
        if (item == null) {
            return Result.error("服务项目不存在");
        }
        return Result.ok(item);
    }

    @PostMapping("/add")
    public Result add(@RequestBody ServiceItem serviceItem) {
        serviceItemService.save(serviceItem);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result update(@RequestBody ServiceItem serviceItem) {
        serviceItemService.updateById(serviceItem);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        serviceItemService.removeById(id);
        return Result.ok();
    }
}
