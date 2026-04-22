package com.yuequge.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.Item;
import com.yuequge.mapper.ItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemMapper itemMapper;

    @GetMapping
    public Result<PageResult<Item>> page(@RequestParam(defaultValue = "1") long page,
                                          @RequestParam(defaultValue = "12") long size,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Item> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(Item::getItemName, keyword);
        }
        w.orderByDesc(Item::getItemId);
        Page<Item> p = itemMapper.selectPage(Page.of(page, size), w);
        return Result.ok(PageResult.of(p.getTotal(), p.getRecords()));
    }

    @GetMapping("/{id}")
    public Result<Item> get(@PathVariable Long id) {
        return Result.ok(itemMapper.selectById(id));
    }
}

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
class AdminItemController {

    private final ItemMapper itemMapper;

    @PostMapping
    public Result<Item> save(@RequestBody Item item) {
        if (item.getItemId() == null) {
            item.setCreateTime(LocalDateTime.now());
            itemMapper.insert(item);
        } else {
            itemMapper.updateById(item);
        }
        return Result.ok(item);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        itemMapper.deleteById(id);
        return Result.ok();
    }
}
