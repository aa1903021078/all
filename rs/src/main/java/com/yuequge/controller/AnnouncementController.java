package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.Announcement;
import com.yuequge.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementMapper mapper;

    /** 前台展示：已发布的公告列表。 */
    @GetMapping
    public Result<List<Announcement>> list() {
        return Result.ok(mapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPriority)
                .orderByDesc(Announcement::getCreatedTime)));
    }
}

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
class AdminAnnouncementController {

    private final AnnouncementMapper mapper;

    @GetMapping
    public Result<PageResult<Announcement>> page(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size) {
        Page<Announcement> p = mapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreatedTime));
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @PostMapping
    public Result<Announcement> save(@RequestBody Announcement a) {
        LocalDateTime now = LocalDateTime.now();
        if (a.getId() == null) {
            a.setCreatedTime(now);
            a.setUpdatedTime(now);
            mapper.insert(a);
        } else {
            a.setUpdatedTime(now);
            mapper.updateById(a);
        }
        return Result.ok(a);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        mapper.deleteById(id);
        return Result.ok();
    }
}
