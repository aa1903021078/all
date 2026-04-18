package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserAddress;
import com.example.service.UserAddressService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户地址前端操作接口
 */
@RestController
@RequestMapping("/userAddress")
public class UserAddressController {

    @Resource
    private UserAddressService userAddressService;

    /**
     * 新增地址
     */
    @PostMapping("/add")
    public Result add(@RequestBody UserAddress userAddress) {
        userAddressService.add(userAddress);
        return Result.success();
    }

    /**
     * 删除地址
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        userAddressService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改地址
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody UserAddress userAddress) {
        userAddressService.updateById(userAddress);
        return Result.success();
    }

    /**
     * 设置默认地址
     */
    @PutMapping("/setDefault/{id}")
    public Result setDefault(@PathVariable Integer id, @RequestParam Integer userId) {
        userAddressService.setDefault(id, userId);
        return Result.success();
    }

    /**
     * 根据ID查询地址
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        UserAddress userAddress = userAddressService.selectById(id);
        return Result.success(userAddress);
    }

    /**
     * 查询用户的所有地址
     */
    @GetMapping("/selectByUser/{userId}")
    public Result selectByUser(@PathVariable Integer userId) {
        List<UserAddress> list = userAddressService.selectByUser(userId);
        return Result.success(list);
    }

    /**
     * 查询用户的默认地址
     */
    @GetMapping("/selectDefault/{userId}")
    public Result selectDefault(@PathVariable Integer userId) {
        UserAddress userAddress = userAddressService.selectDefault(userId);
        return Result.success(userAddress);
    }

}
