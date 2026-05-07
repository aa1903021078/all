package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.Book;
import com.dahaiwuliang.entity.Favorite;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.mapper.FavoriteMapper;
import com.dahaiwuliang.service.BookService;
import com.dahaiwuliang.service.UserService;
import com.dahaiwuliang.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private BookService bookService;
    @Autowired
    private FavoriteMapper favoriteMapper;

    @PostMapping("/wx/login")
    public Result<?> wxLogin(@RequestBody Map<String, String> params) {
        String code = params.get("code");
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }
        User user = userService.wxLogin(code);
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", user);
        return Result.success(data);
    }

    @PostMapping("/admin/login")
    public Result<?> adminLogin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        User admin = userService.adminLogin(username, password);
        if (admin == null) {
            return Result.error("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(admin.getId(), admin.getRole());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", admin);
        return Result.success(data);
    }

    @GetMapping("/user/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/user/update")
    public Result<?> updateUser(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        user.setId(userId);
        user.setRole(null);
        user.setCreditScore(null);
        userService.updateById(user);
        return Result.success();
    }

    @PostMapping("/user/register")
    public Result<?> register(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        User existUser = userService.getById(userId);
        existUser.setStudentId(user.getStudentId());
        existUser.setRealName(user.getRealName());
        existUser.setCollege(user.getCollege());
        existUser.setPhone(user.getPhone());
        userService.updateById(existUser);
        return Result.success();
    }

    @GetMapping("/user/my-books")
    public Result<?> myBooks(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Book> books = bookService.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getUserId, userId).orderByDesc(Book::getCreateTime));
        return Result.success(books);
    }

    @GetMapping("/user/my-favorites")
    public Result<?> myFavorites(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Favorite> favorites = favoriteMapper.selectList(
                new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        return Result.success(favorites);
    }
}
