package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequirePerm;
import com.dahaiwuliang.entity.Category;
import com.dahaiwuliang.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜系分类接口
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public R<List<Category>> list() {
        return R.ok(categoryService.list());
    }

    @PostMapping
    @RequirePerm("shop:manage")
    public R<Category> save(@RequestBody Category category) {
        return R.ok(categoryService.save(category));
    }

    @PutMapping("/{id}")
    @RequirePerm("shop:manage")
    public R<Category> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        return R.ok(categoryService.update(category));
    }

    @DeleteMapping("/{id}")
    @RequirePerm("shop:manage")
    public R<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return R.ok();
    }
}
