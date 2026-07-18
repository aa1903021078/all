package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.RecipeIngredient;
import com.dahaiwuliang.entity.ShoppingItem;
import com.dahaiwuliang.mapper.RecipeIngredientMapper;
import com.dahaiwuliang.mapper.RecipeMapper;
import com.dahaiwuliang.mapper.ShoppingItemMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 食材采购清单(居家做菜闭环)
 */
@Service
public class ShoppingService {

    private final ShoppingItemMapper itemMapper;
    private final RecipeIngredientMapper ingredientMapper;
    private final RecipeMapper recipeMapper;

    public ShoppingService(ShoppingItemMapper itemMapper, RecipeIngredientMapper ingredientMapper,
                           RecipeMapper recipeMapper) {
        this.itemMapper = itemMapper;
        this.ingredientMapper = ingredientMapper;
        this.recipeMapper = recipeMapper;
    }

    public List<ShoppingItem> list() {
        Long userId = UserContext.requireUserId();
        return itemMapper.selectList(new LambdaQueryWrapper<ShoppingItem>()
                .eq(ShoppingItem::getUserId, userId)
                .orderByAsc(ShoppingItem::getStatus)
                .orderByDesc(ShoppingItem::getId));
    }

    public ShoppingItem add(ShoppingItem item) {
        Long userId = UserContext.requireUserId();
        item.setId(null);
        item.setUserId(userId);
        if (item.getStatus() == null) {
            item.setStatus(0);
        }
        itemMapper.insert(item);
        return item;
    }

    /** 从菜谱一键加入采购清单 */
    @Transactional(rollbackFor = Exception.class)
    public List<ShoppingItem> addFromRecipe(Long recipeId) {
        Long userId = UserContext.requireUserId();
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException("菜谱不存在");
        }
        List<RecipeIngredient> ingredients = ingredientMapper.selectList(new LambdaQueryWrapper<RecipeIngredient>()
                .eq(RecipeIngredient::getRecipeId, recipeId).orderByAsc(RecipeIngredient::getSort));
        List<ShoppingItem> result = new ArrayList<>();
        for (RecipeIngredient ing : ingredients) {
            ShoppingItem item = new ShoppingItem();
            item.setUserId(userId);
            item.setName(ing.getName());
            item.setAmount(ing.getAmount());
            item.setRecipeId(recipeId);
            item.setStatus(0);
            itemMapper.insert(item);
            result.add(item);
        }
        return result;
    }

    public ShoppingItem updateItem(ShoppingItem item) {
        Long userId = UserContext.requireUserId();
        ShoppingItem db = itemMapper.selectById(item.getId());
        if (db == null || !userId.equals(db.getUserId())) {
            throw new BusinessException("清单项不存在");
        }
        item.setUserId(null);
        itemMapper.updateById(item);
        return itemMapper.selectById(item.getId());
    }

    public void updateStatus(Long id, Integer status) {
        Long userId = UserContext.requireUserId();
        ShoppingItem db = itemMapper.selectById(id);
        if (db == null || !userId.equals(db.getUserId())) {
            throw new BusinessException("清单项不存在");
        }
        db.setStatus(status);
        itemMapper.updateById(db);
    }

    public void delete(Long id) {
        Long userId = UserContext.requireUserId();
        ShoppingItem db = itemMapper.selectById(id);
        if (db != null && userId.equals(db.getUserId())) {
            itemMapper.deleteById(id);
        }
    }

    /** 清空已核销 */
    public void clearDone() {
        Long userId = UserContext.requireUserId();
        itemMapper.delete(new LambdaQueryWrapper<ShoppingItem>()
                .eq(ShoppingItem::getUserId, userId)
                .eq(ShoppingItem::getStatus, 2));
    }
}
