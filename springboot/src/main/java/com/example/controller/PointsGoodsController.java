package com.example.controller;


import com.example.common.Result;
import com.example.entity.PointsGoods;
import com.example.service.PointsGoodsService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分商品前端操作接口
 */
@RestController
@RequestMapping("/pointsGoods")
public class PointsGoodsController {

    @Resource
    private PointsGoodsService pointsGoodsService;

    /**
     *新增兑换商品
     */
    @PostMapping("/add")
    public Result add(@RequestBody PointsGoods pointsGoods) {
        pointsGoodsService.add(pointsGoods);
        return Result.success();
    }

    /**
     *删除兑换商品
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        pointsGoodsService.deleteById(id);
        return Result.success();
    }

    /**
     *更新兑换商品
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody PointsGoods pointsGoods) {
        pointsGoodsService.updateById(pointsGoods);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        PointsGoods pointsGoods = pointsGoodsService.selectById(id);
        return Result.success(pointsGoods);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(PointsGoods pointsGoods) {
        List<PointsGoods> list = pointsGoodsService.selectAll(pointsGoods);
        return Result.success(list);
    }

    /**
     *分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(PointsGoods pointsGoods,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<PointsGoods> pageInfo = pointsGoodsService.selectPage(pointsGoods, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 查询可兑换的商品（库存>0且上架）
     */
    @GetMapping("/selectAvailable")
    public Result selectAvailable() {
        List<PointsGoods> list = pointsGoodsService.selectAvailable();
        return Result.success(list);
    }
}
