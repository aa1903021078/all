package com.dahaiwuliang.controller;

import com.dahaiwuliang.common.R;
import com.dahaiwuliang.entity.SearchKeyword;
import com.dahaiwuliang.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 全文搜索接口
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public R<Map<String, Object>> search(@RequestParam String keyword) {
        return R.ok(searchService.search(keyword));
    }

    @GetMapping("/hot")
    public R<List<SearchKeyword>> hot(@RequestParam(defaultValue = "10") int limit) {
        return R.ok(searchService.hotKeywords(limit));
    }
}
