package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.entity.UserFavorite;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.BookMapper;
import com.yuequge.mapper.UserFavoriteMapper;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserFavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;

    @GetMapping
    public Result<List<Book>> myFavorites() {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        List<UserFavorite> list = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, u.userId())
                .orderByDesc(UserFavorite::getCreateTime));
        if (list.isEmpty()) return Result.ok(Collections.emptyList());
        List<Long> bookIds = list.stream().map(UserFavorite::getBookId).collect(Collectors.toList());
        return Result.ok(bookMapper.selectBatchIds(bookIds));
    }

    @PostMapping("/{bookId}")
    public Result<Void> add(@PathVariable Long bookId) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, u.userId())
                .eq(UserFavorite::getBookId, bookId));
        if (exists != null && exists > 0) return Result.ok();
        UserFavorite f = new UserFavorite();
        f.setUserId(u.userId());
        f.setBookId(bookId);
        f.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(f);
        return Result.ok();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        favoriteMapper.delete(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, u.userId())
                .eq(UserFavorite::getBookId, bookId));
        return Result.ok();
    }

    @GetMapping("/check/{bookId}")
    public Result<Boolean> check(@PathVariable Long bookId) {
        var u = UserContext.get();
        if (u == null) return Result.ok(false);
        Long exists = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, u.userId())
                .eq(UserFavorite::getBookId, bookId));
        return Result.ok(exists != null && exists > 0);
    }
}
