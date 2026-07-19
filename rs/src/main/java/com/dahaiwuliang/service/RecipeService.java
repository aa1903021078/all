package com.dahaiwuliang.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜谱模块(CRUD / 食材 / 步骤 / 复刻)
 */
@Service
public class RecipeService {

    private final RecipeMapper recipeMapper;
    private final RecipeIngredientMapper ingredientMapper;
    private final RecipeStepMapper stepMapper;
    private final RecipeRepostMapper repostMapper;
    private final CategoryMapper categoryMapper;
    private final UserService userService;
    private final LikeService likeService;
    private final FavoriteService favoriteService;

    public RecipeService(RecipeMapper recipeMapper, RecipeIngredientMapper ingredientMapper,
                         RecipeStepMapper stepMapper, RecipeRepostMapper repostMapper,
                         CategoryMapper categoryMapper, UserService userService,
                         LikeService likeService, FavoriteService favoriteService) {
        this.recipeMapper = recipeMapper;
        this.ingredientMapper = ingredientMapper;
        this.stepMapper = stepMapper;
        this.repostMapper = repostMapper;
        this.categoryMapper = categoryMapper;
        this.userService = userService;
        this.likeService = likeService;
        this.favoriteService = favoriteService;
    }

    /** 分页查询已发布菜谱 */
    public Page<Recipe> pageRecipes(long current, long size, String keyword, Long categoryId,
                                    Integer difficulty, String sort) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getStatus, 1);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Recipe::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Recipe::getCategoryId, categoryId);
        }
        if (difficulty != null) {
            wrapper.eq(Recipe::getDifficulty, difficulty);
        }
        applySort(wrapper, sort);
        Page<Recipe> page = recipeMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), UserContext.getUserId());
        return page;
    }

    private void applySort(LambdaQueryWrapper<Recipe> wrapper, String sort) {
        if (sort == null) {
            sort = "";
        }
        switch (sort) {
            case "hot":
                wrapper.orderByDesc(Recipe::getLikeCount).orderByDesc(Recipe::getViewCount);
                break;
            case "quick":
                wrapper.orderByAsc(Recipe::getCookTime);
                break;
            default:
                wrapper.orderByDesc(Recipe::getId);
        }
    }

    /** 通过食材模糊搜索菜谱 id(用于全文搜索) */
    public List<Long> searchIdsByIngredient(String keyword) {
        if (StrUtil.isBlank(keyword)) {
            return Collections.emptyList();
        }
        List<RecipeIngredient> list = ingredientMapper.selectList(new LambdaQueryWrapper<RecipeIngredient>()
                .like(RecipeIngredient::getName, keyword));
        return list.stream().map(RecipeIngredient::getRecipeId).distinct().collect(Collectors.toList());
    }

    /** 菜谱详情(含食材/步骤) */
    public Recipe detail(Long id) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("菜谱不存在");
        }
        recipe.setViewCount((recipe.getViewCount() == null ? 0 : recipe.getViewCount()) + 1);
        recipeMapper.updateById(recipe);
        enrich(Collections.singletonList(recipe), UserContext.getUserId());
        recipe.setIngredients(ingredientMapper.selectList(new LambdaQueryWrapper<RecipeIngredient>()
                .eq(RecipeIngredient::getRecipeId, id).orderByAsc(RecipeIngredient::getSort)));
        recipe.setSteps(stepMapper.selectList(new LambdaQueryWrapper<RecipeStep>()
                .eq(RecipeStep::getRecipeId, id).orderByAsc(RecipeStep::getSort)));
        return recipe;
    }

    public List<Recipe> recommend(int limit) {
        List<Recipe> recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getStatus, 1).eq(Recipe::getRecommend, 1)
                .orderByDesc(Recipe::getLikeCount).last("limit " + limit));
        enrich(recipes, UserContext.getUserId());
        return recipes;
    }

    public List<Recipe> myRecipes() {
        Long userId = UserContext.requireUserId();
        List<Recipe> recipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getAuthorId, userId).orderByDesc(Recipe::getId));
        enrich(recipes, userId);
        return recipes;
    }

    @Transactional(rollbackFor = Exception.class)
    public Recipe saveRecipe(Recipe recipe) {
        Long userId = UserContext.requireUserId();
        recipe.setId(null);
        recipe.setAuthorId(userId);
        recipe.setStatus(1);
        recipe.setViewCount(0);
        recipe.setLikeCount(0);
        recipe.setFavoriteCount(0);
        if (recipe.getRecommend() == null) {
            recipe.setRecommend(0);
        }
        recipeMapper.insert(recipe);
        saveIngredientsAndSteps(recipe);
        return recipe;
    }

    @Transactional(rollbackFor = Exception.class)
    public Recipe updateRecipe(Recipe recipe) {
        Long userId = UserContext.requireUserId();
        Recipe db = recipeMapper.selectById(recipe.getId());
        if (db == null) {
            throw new BusinessException("菜谱不存在");
        }
        if (!userId.equals(db.getAuthorId()) && !UserContext.get().isAdmin()) {
            throw new BusinessException(403, "只能修改自己的菜谱");
        }
        // 保护统计字段
        recipe.setAuthorId(null);
        recipe.setViewCount(null);
        recipe.setLikeCount(null);
        recipe.setFavoriteCount(null);
        recipeMapper.updateById(recipe);
        // 全量替换食材与步骤
        ingredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>()
                .eq(RecipeIngredient::getRecipeId, recipe.getId()));
        stepMapper.delete(new LambdaQueryWrapper<RecipeStep>()
                .eq(RecipeStep::getRecipeId, recipe.getId()));
        saveIngredientsAndSteps(recipe);
        return recipeMapper.selectById(recipe.getId());
    }

    private void saveIngredientsAndSteps(Recipe recipe) {
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        if (ingredients != null) {
            int i = 0;
            for (RecipeIngredient ing : ingredients) {
                ing.setId(null);
                ing.setRecipeId(recipe.getId());
                ing.setSort(ing.getSort() == null ? i : ing.getSort());
                ingredientMapper.insert(ing);
                i++;
            }
        }
        List<RecipeStep> steps = recipe.getSteps();
        if (steps != null) {
            int i = 0;
            for (RecipeStep step : steps) {
                step.setId(null);
                step.setRecipeId(recipe.getId());
                step.setStepNo(step.getStepNo() == null ? i + 1 : step.getStepNo());
                step.setSort(step.getSort() == null ? i : step.getSort());
                stepMapper.insert(step);
                i++;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRecipe(Long id) {
        Long userId = UserContext.requireUserId();
        Recipe db = recipeMapper.selectById(id);
        if (db == null) {
            return;
        }
        if (!userId.equals(db.getAuthorId()) && !UserContext.get().isAdmin()) {
            throw new BusinessException(403, "只能删除自己的菜谱");
        }
        recipeMapper.deleteById(id);
        ingredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, id));
        stepMapper.delete(new LambdaQueryWrapper<RecipeStep>().eq(RecipeStep::getRecipeId, id));
    }

    /** 后台删除菜谱(内容审核), 无需作者校验, 级联清理食材与步骤 */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        if (recipeMapper.selectById(id) == null) {
            return;
        }
        recipeMapper.deleteById(id);
        ingredientMapper.delete(new LambdaQueryWrapper<RecipeIngredient>().eq(RecipeIngredient::getRecipeId, id));
        stepMapper.delete(new LambdaQueryWrapper<RecipeStep>().eq(RecipeStep::getRecipeId, id));
    }

    public boolean like(Long id) {
        return likeService.toggle("RECIPE", id);
    }

    public boolean favorite(Long id, Long folderId) {
        return favoriteService.toggle("RECIPE", id, folderId);
    }

    // ---------------- 复刻晒图 ----------------

    public List<RecipeRepost> listReposts(Long recipeId) {
        List<RecipeRepost> reposts = repostMapper.selectList(new LambdaQueryWrapper<RecipeRepost>()
                .eq(RecipeRepost::getRecipeId, recipeId).orderByDesc(RecipeRepost::getId));
        Set<Long> userIds = reposts.stream().map(RecipeRepost::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userService.mapByIds(userIds);
        for (RecipeRepost rp : reposts) {
            SysUser u = userMap.get(rp.getUserId());
            if (u != null) {
                rp.setUserName(u.getNickname());
                rp.setUserAvatar(u.getAvatar());
            }
        }
        return reposts;
    }

    @Transactional(rollbackFor = Exception.class)
    public RecipeRepost addRepost(RecipeRepost repost) {
        Long userId = UserContext.requireUserId();
        Recipe recipe = recipeMapper.selectById(repost.getRecipeId());
        if (recipe == null) {
            throw new BusinessException("菜谱不存在");
        }
        repost.setId(null);
        repost.setUserId(userId);
        repost.setLikeCount(0);
        repost.setCreateTime(LocalDateTime.now());
        repostMapper.insert(repost);
        return repost;
    }

    // ---------------- 后台 ----------------

    public Page<Recipe> adminPage(long current, long size, String keyword, Integer status) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Recipe::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Recipe::getStatus, status);
        }
        wrapper.orderByDesc(Recipe::getId);
        Page<Recipe> page = recipeMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), null);
        return page;
    }

    public void changeStatus(Long id, Integer status) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("菜谱不存在");
        }
        recipe.setStatus(status);
        recipeMapper.updateById(recipe);
    }

    /** 后台编辑菜谱基本信息(标题/封面/描述/分类/时长/难度), 不改动食材与步骤 */
    @Transactional(rollbackFor = Exception.class)
    public Recipe adminUpdateBasic(Recipe recipe) {
        Recipe db = recipeMapper.selectById(recipe.getId());
        if (db == null) {
            throw new BusinessException("菜谱不存在");
        }
        db.setTitle(recipe.getTitle());
        db.setCover(recipe.getCover());
        db.setDescription(recipe.getDescription());
        if (recipe.getCategoryId() != null) {
            db.setCategoryId(recipe.getCategoryId());
        }
        if (recipe.getCookTime() != null) {
            db.setCookTime(recipe.getCookTime());
        }
        if (recipe.getDifficulty() != null) {
            db.setDifficulty(recipe.getDifficulty());
        }
        recipeMapper.updateById(db);
        return recipeMapper.selectById(db.getId());
    }

    public void setRecommend(Long id, Integer recommend) {
        Recipe recipe = recipeMapper.selectById(id);
        if (recipe == null) {
            throw new BusinessException("菜谱不存在");
        }
        recipe.setRecommend(recommend);
        recipeMapper.updateById(recipe);
    }

    /** 回填 分类名 / 作者 / 点赞收藏状态 */
    private void enrich(List<Recipe> recipes, Long userId) {
        if (recipes == null || recipes.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = recipes.stream().map(Recipe::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectBatchIds(categoryIds)
                    .forEach(c -> categoryNames.put(c.getId(), c.getName()));
        }
        Set<Long> authorIds = recipes.stream().map(Recipe::getAuthorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userService.mapByIds(authorIds);

        List<Long> ids = recipes.stream().map(Recipe::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.likedIds("RECIPE", ids, userId);
        Set<Long> favoritedIds = favoriteService.favoritedIds("RECIPE", ids, userId);

        for (Recipe r : recipes) {
            r.setCategoryName(categoryNames.get(r.getCategoryId()));
            SysUser author = userMap.get(r.getAuthorId());
            if (author != null) {
                r.setAuthorName(author.getNickname());
                r.setAuthorAvatar(author.getAvatar());
            }
            r.setLiked(likedIds.contains(r.getId()));
            r.setFavorited(favoritedIds.contains(r.getId()));
        }
    }
}
