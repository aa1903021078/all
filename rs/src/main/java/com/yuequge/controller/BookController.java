package com.yuequge.controller;

import com.yuequge.common.PageResult;
import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.entity.BookContent;
import com.yuequge.service.BookService;
import com.yuequge.service.UserActionService;
import com.yuequge.util.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台图书接口（无需管理员权限，登录用户可访问）。
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final UserActionService userActionService;

    @GetMapping
    public Result<PageResult<Book>> page(@RequestParam(defaultValue = "1") long page,
                                         @RequestParam(defaultValue = "12") long size,
                                         @RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String type) {
        return Result.ok(bookService.page(page, size, keyword, type));
    }

    @GetMapping("/{id}")
    public Result<Book> get(@PathVariable Long id) {
        Book b = bookService.getById(id);
        var u = UserContext.get();
        userActionService.track(u == null ? null : u.userId(), "BOOK", id, "VIEW");
        return Result.ok(b);
    }

    @GetMapping("/{id}/chapters")
    public Result<List<BookContent>> chapters(@PathVariable Long id) {
        return Result.ok(bookService.listChapters(id));
    }

    @GetMapping("/chapters/{chapterId}")
    public Result<BookContent> chapter(@PathVariable Long chapterId) {
        BookContent c = bookService.getChapter(chapterId);
        bookService.incrHeat(c.getBookId());
        var u = UserContext.get();
        userActionService.track(u == null ? null : u.userId(), "BOOK", c.getBookId(), "READ",
                "chapter=" + chapterId);
        return Result.ok(c);
    }
}
