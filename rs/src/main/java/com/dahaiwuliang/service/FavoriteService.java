package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.FavoriteFolder;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.entity.UserFavorite;
import com.dahaiwuliang.mapper.FavoriteFolderMapper;
import com.dahaiwuliang.mapper.RecipeMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import com.dahaiwuliang.mapper.UserFavoriteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 收藏 & 收藏夹(SHOP / RECIPE)
 */
@Service
public class FavoriteService {

    private final UserFavoriteMapper favoriteMapper;
    private final FavoriteFolderMapper folderMapper;
    private final ShopMapper shopMapper;
    private final RecipeMapper recipeMapper;

    public FavoriteService(UserFavoriteMapper favoriteMapper, FavoriteFolderMapper folderMapper,
                           ShopMapper shopMapper, RecipeMapper recipeMapper) {
        this.favoriteMapper = favoriteMapper;
        this.folderMapper = folderMapper;
        this.shopMapper = shopMapper;
        this.recipeMapper = recipeMapper;
    }

    /** 收藏/取消, 返回收藏后的状态 */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(String targetType, Long targetId, Long folderId) {
        Long userId = UserContext.requireUserId();
        UserFavorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));
        if (exist != null) {
            favoriteMapper.deleteById(exist.getId());
            updateRecipeCount(targetType, targetId, -1);
            return false;
        }
        UserFavorite fav = new UserFavorite();
        fav.setUserId(userId);
        fav.setTargetType(targetType);
        fav.setTargetId(targetId);
        fav.setFolderId(folderId == null ? 0L : folderId);
        fav.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(fav);
        updateRecipeCount(targetType, targetId, 1);
        return true;
    }

    private void updateRecipeCount(String targetType, Long targetId, int delta) {
        if ("RECIPE".equals(targetType)) {
            Recipe r = recipeMapper.selectById(targetId);
            if (r != null) {
                int c = (r.getFavoriteCount() == null ? 0 : r.getFavoriteCount()) + delta;
                r.setFavoriteCount(Math.max(0, c));
                recipeMapper.updateById(r);
            }
        }
    }

    public boolean isFavorited(String targetType, Long targetId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = favoriteMapper.selectCount(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .eq(UserFavorite::getTargetId, targetId));
        return count != null && count > 0;
    }

    public Set<Long> favoritedIds(String targetType, Collection<Long> targetIds, Long userId) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<UserFavorite> list = favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                .eq(UserFavorite::getUserId, userId)
                .eq(UserFavorite::getTargetType, targetType)
                .in(UserFavorite::getTargetId, new HashSet<>(targetIds)));
        return list.stream().map(UserFavorite::getTargetId).collect(Collectors.toSet());
    }

    // ---------------- 收藏夹 ----------------

    public List<FavoriteFolder> listFolders(String type) {
        Long userId = UserContext.requireUserId();
        LambdaQueryWrapper<FavoriteFolder> wrapper = new LambdaQueryWrapper<FavoriteFolder>()
                .eq(FavoriteFolder::getUserId, userId);
        if (type != null) {
            wrapper.eq(FavoriteFolder::getType, type);
        }
        return folderMapper.selectList(wrapper.orderByDesc(FavoriteFolder::getId));
    }

    public FavoriteFolder createFolder(FavoriteFolder folder) {
        folder.setId(null);
        folder.setUserId(UserContext.requireUserId());
        folderMapper.insert(folder);
        return folder;
    }

    public void deleteFolder(Long id) {
        Long userId = UserContext.requireUserId();
        FavoriteFolder folder = folderMapper.selectById(id);
        if (folder == null || !userId.equals(folder.getUserId())) {
            throw new BusinessException("收藏夹不存在");
        }
        folderMapper.deleteById(id);
    }

    // ---------------- 我的收藏列表 ----------------

    public List<Shop> myFavoriteShops() {
        List<Long> ids = myFavoriteTargetIds("SHOP");
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Shop> shops = shopMapper.selectBatchIds(ids);
        shops.forEach(s -> s.setFavorited(true));
        return shops;
    }

    public List<Recipe> myFavoriteRecipes() {
        List<Long> ids = myFavoriteTargetIds("RECIPE");
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Recipe> recipes = recipeMapper.selectBatchIds(ids);
        recipes.forEach(r -> r.setFavorited(true));
        return recipes;
    }

    private List<Long> myFavoriteTargetIds(String type) {
        Long userId = UserContext.requireUserId();
        return favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                        .eq(UserFavorite::getUserId, userId)
                        .eq(UserFavorite::getTargetType, type)
                        .orderByDesc(UserFavorite::getId))
                .stream().map(UserFavorite::getTargetId).collect(Collectors.toList());
    }
}
