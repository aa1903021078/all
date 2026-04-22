package com.yuequge.controller.admin;

import com.yuequge.common.Result;
import com.yuequge.entity.Book;
import com.yuequge.entity.BookContent;
import com.yuequge.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 后台图书 / 章节管理。
 */
@RestController
@RequestMapping("/api/admin/books")
@RequiredArgsConstructor
public class AdminBookController {

    private final BookService bookService;

    @PostMapping
    public Result<Book> save(@RequestBody Book book) {
        return Result.ok(bookService.save(book));
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        bookService.remove(id);
        return Result.ok();
    }

    @PostMapping("/chapters")
    public Result<BookContent> saveChapter(@RequestBody BookContent content) {
        return Result.ok(bookService.saveChapter(content));
    }

    @DeleteMapping("/chapters/{chapterId}")
    public Result<Void> removeChapter(@PathVariable Long chapterId) {
        bookService.removeChapter(chapterId);
        return Result.ok();
    }
}
