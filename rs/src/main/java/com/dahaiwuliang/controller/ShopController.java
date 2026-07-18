package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.PageResult;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.common.annotation.RequireRole;
import com.dahaiwuliang.entity.Dish;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.service.ShopService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 探店 / 店铺接口
 */
@RestController
@RequestMapping("/shops")
public class ShopController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // ---------------- 公共浏览 ----------------

    /** 分页查询上架店铺(支持关键词/菜系/排序) */
    @GetMapping
    public R<PageResult<Shop>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) String sort) {
        Page<Shop> page = shopService.pageShops(current, size, keyword, categoryId, sort);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    /** 店铺详情 */
    @GetMapping("/{id}")
    public R<Shop> detail(@PathVariable Long id) {
        return R.ok(shopService.detail(id));
    }

    /** 美食地图点位 */
    @GetMapping("/map")
    public R<List<Shop>> map(@RequestParam(required = false) Long categoryId,
                             @RequestParam(required = false) Boolean onlyLit) {
        return R.ok(shopService.mapShops(categoryId, onlyLit));
    }

    /** 周边美食雷达 */
    @GetMapping("/nearby")
    public R<List<Shop>> nearby(@RequestParam double lng,
                                @RequestParam double lat,
                                @RequestParam(defaultValue = "3") double radius,
                                @RequestParam(required = false) Long categoryId,
                                @RequestParam(required = false) BigDecimal minRating,
                                @RequestParam(required = false) Boolean onlyLit) {
        return R.ok(shopService.nearby(lng, lat, radius, categoryId, minRating, onlyLit));
    }

    /** 首页推荐 */
    @GetMapping("/recommend")
    public R<List<Shop>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return R.ok(shopService.recommend(limit));
    }

    /** 高分榜单 */
    @GetMapping("/ranking")
    public R<List<Shop>> ranking(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(shopService.ranking(limit));
    }

    /** 店铺菜品列表 */
    @GetMapping("/{id}/dishes")
    public R<List<Dish>> dishes(@PathVariable Long id) {
        return R.ok(shopService.listDishes(id));
    }

    // ---------------- 登录用户 ----------------

    /** 点亮店铺 */
    @PostMapping("/{id}/checkin")
    @RequireLogin
    public R<Boolean> checkin(@PathVariable Long id) {
        return R.ok("点亮成功", shopService.checkin(id));
    }

    /** 我点亮的店铺 */
    @GetMapping("/my-checkin")
    @RequireLogin
    public R<List<Shop>> myCheckin() {
        return R.ok(shopService.myCheckinShops());
    }

    // ---------------- 商家管理 ----------------

    /** 我的店铺(商家) */
    @GetMapping("/mine")
    @RequireRole("MERCHANT")
    public R<List<Shop>> mine() {
        return R.ok(shopService.merchantShops(UserContext.requireUserId()));
    }

    /** 商家新增店铺(入驻,默认待审核) */
    @PostMapping("/merchant")
    @RequireRole("MERCHANT")
    public R<Shop> merchantCreate(@RequestBody Shop shop) {
        shop.setStatus(0);
        return R.ok("提交成功, 等待审核", shopService.saveShop(shop, UserContext.requireUserId()));
    }

    /** 商家修改自己的店铺 */
    @PutMapping("/merchant/{id}")
    @RequireRole("MERCHANT")
    public R<Shop> merchantUpdate(@PathVariable Long id, @RequestBody Shop shop) {
        shop.setId(id);
        return R.ok(shopService.updateShop(shop, UserContext.requireUserId()));
    }

    /** 商家维护菜品 */
    @PostMapping("/dishes")
    @RequireRole("MERCHANT")
    public R<Dish> saveDish(@RequestBody Dish dish) {
        return R.ok(shopService.saveDish(dish));
    }

    @PutMapping("/dishes/{id}")
    @RequireRole("MERCHANT")
    public R<Dish> updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        dish.setId(id);
        return R.ok(shopService.updateDish(dish));
    }

    @DeleteMapping("/dishes/{id}")
    @RequireRole("MERCHANT")
    public R<Void> deleteDish(@PathVariable Long id) {
        shopService.deleteDish(id);
        return R.ok();
    }

    // ---------------- 后台管理 ----------------

    /** 后台分页(含全部状态) */
    @GetMapping("/admin/page")
    @RequirePerm("shop:manage")
    public R<PageResult<Shop>> adminPage(@RequestParam(defaultValue = "1") long current,
                                         @RequestParam(defaultValue = "10") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) Long categoryId,
                                         @RequestParam(required = false) Integer status) {
        Page<Shop> page = shopService.adminPage(current, size, keyword, categoryId, status);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    /** 后台新增店铺 */
    @PostMapping("/admin")
    @RequirePerm("shop:manage")
    public R<Shop> adminCreate(@RequestBody Shop shop) {
        return R.ok(shopService.saveShop(shop, null));
    }

    /** 后台修改店铺 */
    @PutMapping("/admin/{id}")
    @RequirePerm("shop:manage")
    public R<Shop> adminUpdate(@PathVariable Long id, @RequestBody Shop shop) {
        shop.setId(id);
        return R.ok(shopService.updateShop(shop, null));
    }

    /** 审核/上下架 */
    @PutMapping("/{id}/status")
    @RequirePerm({"shop:manage", "content:review"})
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        shopService.changeStatus(id, status);
        return R.ok();
    }

    /** 设置首页推荐 */
    @PutMapping("/{id}/recommend")
    @RequirePerm("shop:manage")
    public R<Void> setRecommend(@PathVariable Long id, @RequestParam Integer recommend) {
        shopService.setRecommend(id, recommend);
        return R.ok();
    }

    /** 删除店铺 */
    @DeleteMapping("/{id}")
    @RequirePerm("shop:manage")
    public R<Void> delete(@PathVariable Long id) {
        shopService.deleteShop(id);
        return R.ok();
    }
}
