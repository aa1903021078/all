package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.Book;
import com.dahaiwuliang.entity.BookCategory;
import com.dahaiwuliang.entity.Favorite;
import com.dahaiwuliang.entity.User;
import com.dahaiwuliang.mapper.BookCategoryMapper;
import com.dahaiwuliang.mapper.FavoriteMapper;
import com.dahaiwuliang.service.BookService;
import com.dahaiwuliang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookCategoryMapper categoryMapper;
    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private UserService userService;

    @PostMapping("/book/publish")
    public Result<?> publish(HttpServletRequest request, @RequestBody Book book) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user.getCreditScore() < 60) {
            return Result.error("信用分过低，无法发布图书");
        }
        book.setUserId(userId);
        book.setStatus(0); // 待审核
        book.setViewCount(0);
        bookService.save(book);
        return Result.success(book);
    }

    @PutMapping("/book/update")
    public Result<?> update(HttpServletRequest request, @RequestBody Book book) {
        Long userId = (Long) request.getAttribute("userId");
        Book existBook = bookService.getById(book.getId());
        if (existBook == null || !existBook.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        book.setUserId(null);
        book.setStatus(null);
        bookService.updateById(book);
        return Result.success();
    }

    @PutMapping("/book/status")
    public Result<?> updateStatus(HttpServletRequest request, @RequestBody Book book) {
        Long userId = (Long) request.getAttribute("userId");
        Book existBook = bookService.getById(book.getId());
        if (existBook == null || !existBook.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        existBook.setStatus(book.getStatus());
        bookService.updateById(existBook);
        return Result.success();
    }

    @DeleteMapping("/book/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        Book book = bookService.getById(id);
        if (book == null || !book.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        bookService.removeById(id);
        return Result.success();
    }

    @GetMapping("/book/list")
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String grade,
                          @RequestParam(required = false) String major) {
        IPage<Book> result = bookService.searchBooks(page, size, categoryId, keyword, grade, major);
        return Result.success(result);
    }

    @GetMapping("/book/{id}")
    public Result<?> detail(@PathVariable Long id) {
        Book book = bookService.getById(id);
        if (book == null) {
            return Result.error("图书不存在");
        }
        // 增加浏览量
        book.setViewCount(book.getViewCount() + 1);
        bookService.updateById(book);
        return Result.success(book);
    }

    @GetMapping("/book/recommend")
    public Result<?> recommend() {
        List<Book> books = bookService.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1)
                .orderByDesc(Book::getViewCount)
                .last("LIMIT 10"));
        return Result.success(books);
    }

    @GetMapping("/book/hot")
    public Result<?> hot() {
        List<Book> books = bookService.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1)
                .orderByDesc(Book::getViewCount)
                .last("LIMIT 20"));
        return Result.success(books);
    }

    @GetMapping("/book/latest")
    public Result<?> latest() {
        List<Book> books = bookService.list(new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1)
                .orderByDesc(Book::getCreateTime)
                .last("LIMIT 20"));
        return Result.success(books);
    }

    @GetMapping("/category/list")
    public Result<?> categoryList() {
        List<BookCategory> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<BookCategory>().orderByAsc(BookCategory::getSort));
        return Result.success(categories);
    }

    @PostMapping("/favorite/add")
    public Result<?> addFavorite(HttpServletRequest request, @RequestBody Favorite favorite) {
        Long userId = (Long) request.getAttribute("userId");
        favorite.setUserId(userId);
        // 检查是否已收藏
        Favorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getBookId, favorite.getBookId()));
        if (exist != null) {
            return Result.error("已收藏");
        }
        favoriteMapper.insert(favorite);
        return Result.success();
    }

    @DeleteMapping("/favorite/{bookId}")
    public Result<?> removeFavorite(HttpServletRequest request, @PathVariable Long bookId) {
        Long userId = (Long) request.getAttribute("userId");
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId));
        return Result.success();
    }
}
