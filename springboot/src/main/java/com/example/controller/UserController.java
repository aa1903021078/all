package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 普通用户前端操作接口
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        userService.register(user);
        return Result.success();
    }


    /**
     * 新增数据 （管理员添加用户）
     * @param user 参数的对象
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        userService.add(user);     //Alt+Enter创建add方法
        return Result.success();
    }

    /**
     * 删除数据
     * 接口路径:/user/delete/1   {id}是路径参数id
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        userService.deleteById(id);
        return Result.success();
    }

    /**
     * 更新数据
     * @param user 参数的对象  编辑后的数据
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        userService.updateById(user);     //Alt+Enter创建add方法
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(User user) {
        List<User> list = userService.selectAll(user);
        return Result.success(list);
    }


    /**
     * @param pageNum 当前页码
     * @param pageSize 每页展示的个数
     * @return 分页数据
     * 接口的请求方式：http://localhost:9090/user/selectPage?pageNum=1&pageSize=10
     */
    @GetMapping("selectPage")
    public Result selectPage(User user,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name) {
        PageInfo<User> pageInfo = userService.selectPage(user,pageNum, pageSize,name);
        return Result.success(pageInfo);
    }

    /**
     * 查询用户个人信息（包含统计）
     */
    @GetMapping("/profile/{id}")
    public Result getUserProfile(@PathVariable Integer id) {
        User user = userService.getUserProfile(id);
        return Result.success(user);
    }

    /**
     * 更新用户积分
     */
    @PutMapping("/updatePoints")
    public Result updatePoints(@RequestParam Integer userId, @RequestParam Integer points) {
        userService.updatePoints(userId, points);
        return Result.success();
    }
}
