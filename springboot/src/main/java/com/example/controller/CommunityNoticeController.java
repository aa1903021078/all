package com.example.controller;


import com.example.common.Result;
import com.example.entity.CommunityNotice;
import com.example.service.CommunityNoticeService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社区公告前端操作接口
 */
@RestController
@RequestMapping("/notice")
public class CommunityNoticeController {

    @Resource
    private CommunityNoticeService communityNoticeService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody CommunityNotice notice) {
        communityNoticeService.add(notice);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        communityNoticeService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody CommunityNotice communityNotice) {
        communityNoticeService.updateById(communityNotice);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        CommunityNotice communityNotice = communityNoticeService.selectById(id);
        return Result.success(communityNotice);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(CommunityNotice communityNotice) {
        List<CommunityNotice> list = communityNoticeService.selectAll(communityNotice);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(CommunityNotice communityNotice,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<CommunityNotice> pageInfo = communityNoticeService.selectPage(communityNotice, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 查询置顶公告
     */
    @GetMapping("/selectTop")
    public Result selectTop() {
        List<CommunityNotice> list = communityNoticeService.selectTop();
        return Result.success(list);
    }

    /**
     * 查询已发布的公告（前端展示用）
     */
    @GetMapping("/selectPublished")
    public Result selectPublished(@RequestParam(required = false) Integer type) {
        List<CommunityNotice> list = communityNoticeService.selectPublished(type);
        return Result.success(list);
    }

    /**
     * 增加浏览次数
     */
    @PutMapping("/increaseView/{id}")
    public Result increaseView(@PathVariable Integer id) {
        communityNoticeService.increaseView(id);
        return Result.success();
    }

    /**
     * 设置置顶
     */
    @PutMapping("/setTop/{id}")
    public Result setTop(@PathVariable Integer id, @RequestParam Integer isTop) {
        communityNoticeService.setTop(id, isTop);
        return Result.success();
    }
}
