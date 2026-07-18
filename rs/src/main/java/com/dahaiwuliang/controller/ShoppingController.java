package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.entity.ShoppingItem;
import com.dahaiwuliang.service.ShoppingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采购清单接口
 */
@RestController
@RequestMapping("/shopping")
@RequireLogin
public class ShoppingController {

    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }

    @GetMapping
    public R<List<ShoppingItem>> list() {
        return R.ok(shoppingService.list());
    }

    @PostMapping
    public R<ShoppingItem> add(@RequestBody ShoppingItem item) {
        return R.ok(shoppingService.add(item));
    }

    @PostMapping("/from-recipe/{recipeId}")
    public R<List<ShoppingItem>> addFromRecipe(@PathVariable Long recipeId) {
        return R.ok("已加入采购清单", shoppingService.addFromRecipe(recipeId));
    }

    @PutMapping("/{id}")
    public R<ShoppingItem> update(@PathVariable Long id, @RequestBody ShoppingItem item) {
        item.setId(id);
        return R.ok(shoppingService.updateItem(item));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        shoppingService.updateStatus(id, status);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        shoppingService.delete(id);
        return R.ok();
    }

    @DeleteMapping("/done")
    public R<Void> clearDone() {
        shoppingService.clearDone();
        return R.ok();
    }
}
