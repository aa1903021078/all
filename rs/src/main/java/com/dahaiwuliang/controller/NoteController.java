package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.PageResult;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.entity.Note;
import com.dahaiwuliang.service.NoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 探店笔记接口
 */
@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public R<PageResult<Note>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Long shopId,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String sort) {
        Page<Note> page = noteService.pageNotes(current, size, shopId, keyword, sort);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/feed")
    public R<List<Note>> feed(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(noteService.feed(limit));
    }

    @GetMapping("/mine")
    @RequireLogin
    public R<List<Note>> mine() {
        return R.ok(noteService.myNotes());
    }

    @GetMapping("/shop/{shopId}")
    public R<List<Note>> byShop(@PathVariable Long shopId) {
        return R.ok(noteService.listByShop(shopId));
    }

    @GetMapping("/{id}")
    public R<Note> detail(@PathVariable Long id) {
        return R.ok(noteService.detail(id));
    }

    @PostMapping
    @RequireLogin
    public R<Note> create(@RequestBody Note note) {
        return R.ok("发布成功", noteService.saveNote(note));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public R<Void> delete(@PathVariable Long id) {
        noteService.deleteNote(id);
        return R.ok();
    }

    @PostMapping("/{id}/like")
    @RequireLogin
    public R<Boolean> like(@PathVariable Long id) {
        return R.ok(noteService.like(id));
    }

    // ---------------- 后台 / 审核 ----------------

    @GetMapping("/admin/page")
    @RequirePerm({"content:review", "content:manage"})
    public R<PageResult<Note>> adminPage(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Integer status) {
        Page<Note> page = noteService.adminPage(current, size, keyword, status);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PutMapping("/{id}/status")
    @RequirePerm({"content:review", "content:manage"})
    public R<Void> review(@PathVariable Long id, @RequestParam Integer status) {
        noteService.review(id, status);
        return R.ok();
    }

    @PutMapping("/{id}/recommend")
    @RequirePerm("content:manage")
    public R<Void> setRecommend(@PathVariable Long id, @RequestParam Integer recommend) {
        noteService.setRecommend(id, recommend);
        return R.ok();
    }
}
