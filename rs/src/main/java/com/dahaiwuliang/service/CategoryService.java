package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.entity.Category;
import com.dahaiwuliang.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜系分类
 */
@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> list() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));
    }

    public Category save(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Category category) {
        categoryMapper.updateById(category);
        return category;
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
