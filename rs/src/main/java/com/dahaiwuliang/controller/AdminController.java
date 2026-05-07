package com.dahaiwuliang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.dto.Result;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import com.dahaiwuliang.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private BookService bookService;
    @Autowired
    private BookCategoryMapper categoryMapper;
    @Autowired
    private ExchangeOrderService exchangeOrderService;
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private AnnouncementMapper announcementMapper;

    // ========== 用户管理 ==========
    @GetMapping("/user/list")
    public Result<?> userList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getStudentId, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> result = userService.page(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @PutMapping("/user/verify/{id}")
    public Result<?> verifyUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(1);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/ban/{id}")
    public Result<?> banUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(2);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/unban/{id}")
    public Result<?> unbanUser(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) return Result.error("用户不存在");
        user.setStatus(1);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/credit")
    public Result<?> updateCredit(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        Integer creditScore = Integer.valueOf(params.get("creditScore").toString());
        User user = userService.getById(userId);
        if (user == null) return Result.error("用户不存在");
        user.setCreditScore(creditScore);
        userService.updateById(user);
        return Result.success();
    }

    // ========== 图书管理 ==========
    @GetMapping("/book/list")
    public Result<?> bookList(@RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Book::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        IPage<Book> result = bookService.page(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @PutMapping("/book/audit/{id}")
    public Result<?> auditBook(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Book book = bookService.getById(id);
        if (book == null) return Result.error("图书不存在");
        book.setStatus(params.get("status")); // 1-通过 2-拒绝
        bookService.updateById(book);
        return Result.success();
    }

    @DeleteMapping("/book/{id}")
    public Result<?> deleteBook(@PathVariable Long id) {
        bookService.removeById(id);
        return Result.success();
    }

    // ========== 分类管理 ==========
    @GetMapping("/category/list")
    public Result<?> categoryList() {
        List<BookCategory> list = categoryMapper.selectList(
                new LambdaQueryWrapper<BookCategory>().orderByAsc(BookCategory::getSort));
        return Result.success(list);
    }

    @PostMapping("/category/add")
    public Result<?> addCategory(@RequestBody BookCategory category) {
        categoryMapper.insert(category);
        return Result.success();
    }

    @PutMapping("/category/update")
    public Result<?> updateCategory(@RequestBody BookCategory category) {
        categoryMapper.updateById(category);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryMapper.deleteById(id);
        return Result.success();
    }

    // ========== 订单管理 ==========
    @GetMapping("/exchange/list")
    public Result<?> exchangeList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ExchangeOrder::getStatus, status);
        }
        wrapper.orderByDesc(ExchangeOrder::getCreateTime);
        IPage<ExchangeOrder> result = exchangeOrderService.page(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @PutMapping("/exchange/handle/{id}")
    public Result<?> handleExchange(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ExchangeOrder order = exchangeOrderService.getById(id);
        if (order == null) return Result.error("订单不存在");
        Integer newStatus = Integer.valueOf(params.get("status").toString());
        order.setStatus(newStatus);
        exchangeOrderService.updateById(order);
        return Result.success();
    }

    // ========== 举报管理 ==========
    @GetMapping("/report/list")
    public Result<?> reportList(@RequestParam(defaultValue = "1") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreateTime);
        IPage<Report> result = reportMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(result);
    }

    @PutMapping("/report/handle/{id}")
    public Result<?> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Report report = reportMapper.selectById(id);
        if (report == null) return Result.error("举报不存在");
        report.setStatus(Integer.valueOf(params.get("status").toString()));
        report.setHandleResult(params.get("handleResult") != null ? params.get("handleResult").toString() : null);
        reportMapper.updateById(report);
        return Result.success();
    }

    // ========== 公告管理 ==========
    @GetMapping("/announcement/list")
    public Result<?> announcementList() {
        List<Announcement> list = announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
        return Result.success(list);
    }

    @PostMapping("/announcement/add")
    public Result<?> addAnnouncement(HttpServletRequest request, @RequestBody Announcement announcement) {
        Long userId = (Long) request.getAttribute("userId");
        announcement.setAdminId(userId);
        announcement.setStatus(1);
        announcementMapper.insert(announcement);
        return Result.success();
    }

    @PutMapping("/announcement/update")
    public Result<?> updateAnnouncement(@RequestBody Announcement announcement) {
        announcementMapper.updateById(announcement);
        return Result.success();
    }

    @DeleteMapping("/announcement/{id}")
    public Result<?> deleteAnnouncement(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Result.success();
    }

    // ========== 数据统计 ==========
    @GetMapping("/stats/overview")
    public Result<?> statsOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userService.count());
        stats.put("bookCount", bookService.count());
        stats.put("exchangeCount", exchangeOrderService.count());
        stats.put("successCount", exchangeOrderService.count(
                new LambdaQueryWrapper<ExchangeOrder>().eq(ExchangeOrder::getStatus, 4)));
        long totalExchange = exchangeOrderService.count();
        long successExchange = exchangeOrderService.count(
                new LambdaQueryWrapper<ExchangeOrder>().eq(ExchangeOrder::getStatus, 4));
        stats.put("successRate", totalExchange > 0 ? (double) successExchange / totalExchange * 100 : 0);
        return Result.success(stats);
    }
}
