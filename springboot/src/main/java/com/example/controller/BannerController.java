package com.example.controller;

import com.example.common.Result;
import com.example.entity.Banner;
import com.example.service.BannerService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图前端操作接口
 */
@RestController
@RequestMapping("/banner")
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        bannerService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Banner banner = bannerService.selectById(id);
        return Result.success(banner);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Banner banner) {
        List<Banner> list = bannerService.selectAll(banner);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Banner banner,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Banner> page = bannerService.selectPage(banner, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 前端接口：获取已发布的轮播图列表
     * GET /banner/selectPublished
     */
    @GetMapping("/selectPublished")
    public Result selectPublished() {
        List<Banner> list = bannerService.getPublishedBanners();
        return Result.success(list);
    }

    /**
     * 上架/下架
     */
    @PutMapping("/toggleStatus/{id}")
    public Result toggleStatus(@PathVariable Integer id, @RequestParam Integer status) {
        bannerService.toggleStatus(id, status);
        return Result.success();
    }

}
