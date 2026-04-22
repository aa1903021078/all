package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.dto.BookCommentVO;
import com.yuequge.entity.BookComment;
import com.yuequge.exception.BizException;
import com.yuequge.service.CommentService;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/book/{bookId}")
    public Result<List<BookCommentVO>> list(@PathVariable Long bookId) {
        return Result.ok(commentService.listByBook(bookId));
    }

    @PostMapping
    public Result<BookComment> create(@RequestBody Map<String, Object> body) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        Long bookId = ((Number) body.get("bookId")).longValue();
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") == null ? null : ((Number) body.get("parentId")).longValue();
        return Result.ok(commentService.create(bookId, u.userId(), content, parentId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        var u = UserContext.get();
        if (u == null) throw new BizException(401, "未登录");
        commentService.delete(id, u.userId(), u.isAdmin());
        return Result.ok();
    }
}
