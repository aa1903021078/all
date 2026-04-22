package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.exception.BizException;
import com.yuequge.service.RecommendService;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 推荐系统 HTTP 接口。
 */
@RestController
@RequiredArgsConstructor
public class RecommendController {

    private final RecommendService recommendService;

    /** 登录用户个性化推荐；冷启动用户回退热榜。 */
    @GetMapping("/api/recommend/my")
    public Result<List<Book>> my(@RequestParam(defaultValue = "10") int n) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        return Result.ok(recommendService.recommendBooks(u.userId(), Math.max(1, Math.min(n, 50))));
    }

    /** 看过此书的人还看了（无需登录）。 */
    @GetMapping("/api/public/recommend/similar/{bookId}")
    public Result<List<Book>> similar(@PathVariable Long bookId,
                                      @RequestParam(defaultValue = "6") int n) {
        return Result.ok(recommendService.similarBooks(bookId, Math.max(1, Math.min(n, 30))));
    }

    /** 管理员手动重建相似度矩阵（演示 Spark 离线作业接入点）。 */
    @PostMapping("/api/admin/recommend/rebuild")
    public Result<RecommendService.Stats> rebuild() {
        var u = UserContext.get();
        if (u == null || !u.isAdmin()) throw new BizException(403, "无权访问");
        return Result.ok(recommendService.rebuild());
    }

    @GetMapping("/api/admin/recommend/stats")
    public Result<RecommendService.Stats> stats() {
        var u = UserContext.get();
        if (u == null || !u.isAdmin()) throw new BizException(403, "无权访问");
        return Result.ok(recommendService.currentStats());
    }
}
