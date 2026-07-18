package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.entity.Comment;
import com.dahaiwuliang.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public R<List<Comment>> list(@RequestParam String targetType, @RequestParam Long targetId) {
        return R.ok(commentService.listTree(targetType, targetId));
    }

    @PostMapping
    @RequireLogin
    public R<Comment> add(@RequestBody Comment comment) {
        return R.ok("评论成功", commentService.add(comment));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public R<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/like")
    @RequireLogin
    public R<Boolean> like(@PathVariable Long id) {
        return R.ok(commentService.like(id));
    }
}
