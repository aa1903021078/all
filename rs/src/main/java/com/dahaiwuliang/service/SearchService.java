package com.dahaiwuliang.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.SearchKeyword;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.mapper.RecipeMapper;
import com.dahaiwuliang.mapper.SearchKeywordMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全文搜索(店铺名 / 菜谱名 / 食材模糊) + 热词
 */
@Service
public class SearchService {

    private final ShopMapper shopMapper;
    private final RecipeMapper recipeMapper;
    private final RecipeService recipeService;
    private final SearchKeywordMapper keywordMapper;

    public SearchService(ShopMapper shopMapper, RecipeMapper recipeMapper,
                         RecipeService recipeService, SearchKeywordMapper keywordMapper) {
        this.shopMapper = shopMapper;
        this.recipeMapper = recipeMapper;
        this.recipeService = recipeService;
        this.keywordMapper = keywordMapper;
    }

    /** 聚合搜索 */
    public Map<String, Object> search(String keyword) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (StrUtil.isBlank(keyword)) {
            result.put("shops", new ArrayList<>());
            result.put("recipes", new ArrayList<>());
            return result;
        }
        recordKeyword(keyword);

        // 店铺: 名称/地址
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .and(w -> w.like(Shop::getName, keyword).or().like(Shop::getAddress, keyword))
                .last("limit 20"));

        // 菜谱: 标题 或 食材命中
        List<Long> ingredientRecipeIds = recipeService.searchIdsByIngredient(keyword);
        LambdaQueryWrapper<Recipe> recipeWrapper = new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getStatus, 1)
                .and(w -> {
                    w.like(Recipe::getTitle, keyword);
                    if (!ingredientRecipeIds.isEmpty()) {
                        w.or().in(Recipe::getId, ingredientRecipeIds);
                    }
                })
                .last("limit 20");
        List<Recipe> recipes = recipeMapper.selectList(recipeWrapper);

        result.put("shops", shops);
        result.put("recipes", recipes);
        return result;
    }

    public List<SearchKeyword> hotKeywords(int limit) {
        return keywordMapper.selectList(new LambdaQueryWrapper<SearchKeyword>()
                .orderByDesc(SearchKeyword::getCount).last("limit " + limit));
    }

    /** 记录/累加搜索热词 */
    private void recordKeyword(String keyword) {
        String kw = keyword.trim();
        if (kw.length() > 32) {
            kw = kw.substring(0, 32);
        }
        final String fkw = kw;
        SearchKeyword exist = keywordMapper.selectOne(new LambdaQueryWrapper<SearchKeyword>()
                .eq(SearchKeyword::getKeyword, fkw).last("limit 1"));
        if (exist != null) {
            exist.setCount((exist.getCount() == null ? 0 : exist.getCount()) + 1);
            keywordMapper.updateById(exist);
        } else {
            SearchKeyword sk = new SearchKeyword();
            sk.setKeyword(fkw);
            sk.setCount(1);
            keywordMapper.insert(sk);
        }
    }
}
