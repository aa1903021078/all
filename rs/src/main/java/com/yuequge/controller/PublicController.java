package com.yuequge.controller;

import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公开接口（无需登录）：首页用。
 */
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final BookService bookService;

    @GetMapping("/hot-books")
    public Result<List<Book>> hotBooks(@RequestParam(defaultValue = "8") int limit) {
        return Result.ok(bookService.hotList(limit));
    }

    @GetMapping("/stats")
    public Result<Object> stats() {
        return Result.ok(bookService.stats());
    }
}
