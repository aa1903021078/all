package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.PageResult;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.RecipeRepost;
import com.dahaiwuliang.service.RecipeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜谱接口
 */
@RestController
@RequestMapping("/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping
    public R<PageResult<Recipe>> page(@RequestParam(defaultValue = "1") long current,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Long categoryId,
                                      @RequestParam(required = false) Integer difficulty,
                                      @RequestParam(required = false) String sort) {
        Page<Recipe> page = recipeService.pageRecipes(current, size, keyword, categoryId, difficulty, sort);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @GetMapping("/recommend")
    public R<List<Recipe>> recommend(@RequestParam(defaultValue = "6") int limit) {
        return R.ok(recipeService.recommend(limit));
    }

    @GetMapping("/mine")
    @RequireLogin
    public R<List<Recipe>> mine() {
        return R.ok(recipeService.myRecipes());
    }

    @GetMapping("/{id}")
    public R<Recipe> detail(@PathVariable Long id) {
        return R.ok(recipeService.detail(id));
    }

    @PostMapping
    @RequireLogin
    public R<Recipe> create(@RequestBody Recipe recipe) {
        return R.ok("发布成功", recipeService.saveRecipe(recipe));
    }

    @PutMapping("/{id}")
    @RequireLogin
    public R<Recipe> update(@PathVariable Long id, @RequestBody Recipe recipe) {
        recipe.setId(id);
        return R.ok(recipeService.updateRecipe(recipe));
    }

    @DeleteMapping("/{id}")
    @RequireLogin
    public R<Void> delete(@PathVariable Long id) {
        recipeService.deleteRecipe(id);
        return R.ok();
    }

    @PostMapping("/{id}/like")
    @RequireLogin
    public R<Boolean> like(@PathVariable Long id) {
        return R.ok(recipeService.like(id));
    }

    @PostMapping("/{id}/favorite")
    @RequireLogin
    public R<Boolean> favorite(@PathVariable Long id,
                               @RequestParam(required = false) Long folderId) {
        return R.ok(recipeService.favorite(id, folderId));
    }

    // ---------------- 复刻晒图 ----------------

    @GetMapping("/{id}/reposts")
    public R<List<RecipeRepost>> reposts(@PathVariable Long id) {
        return R.ok(recipeService.listReposts(id));
    }

    @PostMapping("/{id}/reposts")
    @RequireLogin
    public R<RecipeRepost> addRepost(@PathVariable Long id, @RequestBody RecipeRepost repost) {
        repost.setRecipeId(id);
        return R.ok("晒图成功", recipeService.addRepost(repost));
    }

    // ---------------- 后台 ----------------

    @GetMapping("/admin/page")
    @RequirePerm({"content:manage", "content:review"})
    public R<PageResult<Recipe>> adminPage(@RequestParam(defaultValue = "1") long current,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Integer status) {
        Page<Recipe> page = recipeService.adminPage(current, size, keyword, status);
        return R.ok(PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PutMapping("/{id}/status")
    @RequirePerm({"content:manage", "content:review"})
    public R<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        recipeService.changeStatus(id, status);
        return R.ok();
    }

    @PutMapping("/{id}/recommend")
    @RequirePerm("content:manage")
    public R<Void> setRecommend(@PathVariable Long id, @RequestParam Integer recommend) {
        recipeService.setRecommend(id, recommend);
        return R.ok();
    }
}
