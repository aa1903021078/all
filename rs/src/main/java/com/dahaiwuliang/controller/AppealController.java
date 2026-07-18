package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.common.annotation.RequireRole;
import com.dahaiwuliang.entity.NoteAppeal;
import com.dahaiwuliang.service.AppealService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 差评申诉接口: 商家提交/查看, 平台处理
 */
@RestController
@RequestMapping("/appeals")
public class AppealController {

    private final AppealService appealService;

    public AppealController(AppealService appealService) {
        this.appealService = appealService;
    }

    /** 商家提交申诉 */
    @PostMapping
    @RequireRole("MERCHANT")
    public R<NoteAppeal> submit(@RequestBody NoteAppeal appeal) {
        return R.ok("申诉已提交", appealService.submit(appeal));
    }

    /** 商家查看自己的申诉 */
    @GetMapping("/mine")
    @RequireRole("MERCHANT")
    public R<List<NoteAppeal>> mine() {
        return R.ok(appealService.myAppeals());
    }

    /** 平台查看全部申诉 */
    @GetMapping("/admin")
    @RequirePerm({"content:review", "content:manage"})
    public R<List<NoteAppeal>> adminList(@RequestParam(required = false) Integer status) {
        return R.ok(appealService.adminList(status));
    }

    /** 平台处理申诉 */
    @PutMapping("/{id}/handle")
    @RequirePerm({"content:review", "content:manage"})
    public R<Void> handle(@PathVariable Long id,
                          @RequestParam Integer status,
                          @RequestParam(required = false) String reply) {
        appealService.handle(id, status, reply);
        return R.ok();
    }
}
