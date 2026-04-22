package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.Result;
import com.dahaiwuliang.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/package")
public class PackageController {

    @Autowired
    private PackageService packageService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) String name) {
        Page<com.dahaiwuliang.entity.Package> page = packageService.list(pageNum, pageSize, status, name);
        return Result.ok(page);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        com.dahaiwuliang.entity.Package pkg = packageService.getById(id);
        if (pkg == null) {
            return Result.error("套餐不存在");
        }
        return Result.ok(pkg);
    }

    @PostMapping("/add")
    public Result add(@RequestBody com.dahaiwuliang.entity.Package pkg) {
        packageService.save(pkg);
        return Result.ok();
    }

    @PutMapping("/update")
    public Result update(@RequestBody com.dahaiwuliang.entity.Package pkg) {
        packageService.updateById(pkg);
        return Result.ok();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        packageService.removeById(id);
        return Result.ok();
    }
}
