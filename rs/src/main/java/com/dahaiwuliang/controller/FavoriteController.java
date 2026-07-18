package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.common.annotation.RequireLogin;
import com.dahaiwuliang.entity.FavoriteFolder;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.service.FavoriteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏 & 收藏夹接口
 */
@RestController
@RequestMapping("/favorites")
@RequireLogin
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /** 收藏/取消(SHOP / RECIPE) */
    @PostMapping("/toggle")
    public R<Boolean> toggle(@RequestParam String targetType,
                             @RequestParam Long targetId,
                             @RequestParam(required = false) Long folderId) {
        return R.ok(favoriteService.toggle(targetType, targetId, folderId));
    }

    @GetMapping("/shops")
    public R<List<Shop>> shops() {
        return R.ok(favoriteService.myFavoriteShops());
    }

    @GetMapping("/recipes")
    public R<List<Recipe>> recipes() {
        return R.ok(favoriteService.myFavoriteRecipes());
    }

    @GetMapping("/folders")
    public R<List<FavoriteFolder>> folders(@RequestParam(required = false) String type) {
        return R.ok(favoriteService.listFolders(type));
    }

    @PostMapping("/folders")
    public R<FavoriteFolder> createFolder(@RequestBody FavoriteFolder folder) {
        return R.ok(favoriteService.createFolder(folder));
    }

    @DeleteMapping("/folders/{id}")
    public R<Void> deleteFolder(@PathVariable Long id) {
        favoriteService.deleteFolder(id);
        return R.ok();
    }
}
