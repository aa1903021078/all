package com.yuequge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.entity.Book;
import com.yuequge.entity.BookContent;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.BookContentMapper;
import com.yuequge.mapper.BookMapper;
import com.yuequge.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final BookContentMapper bookContentMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String HOT_CACHE_KEY = "yuequge:book:hot";

    @Override
    public PageResult<Book> page(long page, long size, String keyword, String type) {
        LambdaQueryWrapper<Book> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(Book::getName, keyword).or().like(Book::getDescription, keyword);
        }
        if (type != null && !type.isBlank()) {
            w.eq(Book::getType, type);
        }
        w.isNotNull(Book::getName).orderByDesc(Book::getId);
        Page<Book> p = bookMapper.selectPage(Page.of(page, size), w);
        return PageResult.of(p.getTotal(), p.getRecords());
    }

    @Override
    public Book getById(Long id) {
        Book b = bookMapper.selectById(id);
        if (b == null) throw new BizException(404, "图书不存在");
        return b;
    }

    @Override
    public List<BookContent> listChapters(Long bookId) {
        return bookContentMapper.selectList(new LambdaQueryWrapper<BookContent>()
                .eq(BookContent::getBookId, bookId)
                .select(BookContent::getId, BookContent::getBookId,
                        BookContent::getChapterNumber, BookContent::getChapterTitle)
                .orderByAsc(BookContent::getChapterNumber));
    }

    @Override
    public BookContent getChapter(Long chapterId) {
        BookContent c = bookContentMapper.selectById(chapterId);
        if (c == null) throw new BizException(404, "章节不存在");
        return c;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Book> hotList(int limit) {
        try {
            Object cached = redisTemplate.opsForValue().get(HOT_CACHE_KEY);
            if (cached instanceof List<?> list && !list.isEmpty()) {
                return (List<Book>) cached;
            }
        } catch (Exception e) {
            log.debug("redis hot cache miss: {}", e.getMessage());
        }
        List<Book> list = bookMapper.selectList(new LambdaQueryWrapper<Book>()
                .isNotNull(Book::getName)
                .orderByDesc(Book::getHeat)
                .orderByDesc(Book::getId)
                .last("limit " + Math.max(1, Math.min(limit, 50))));
        try {
            redisTemplate.opsForValue().set(HOT_CACHE_KEY, list, Duration.ofMinutes(5));
        } catch (Exception e) {
            log.debug("redis set hot cache failed: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public Book save(Book book) {
        if (book.getHeat() == null) book.setHeat(0L);
        if (book.getId() == null) {
            bookMapper.insert(book);
        } else {
            bookMapper.updateById(book);
        }
        clearHotCache();
        return book;
    }

    @Override
    public void remove(Long id) {
        bookMapper.deleteById(id);
        clearHotCache();
    }

    @Override
    public BookContent saveChapter(BookContent content) {
        if (content.getId() == null) {
            bookContentMapper.insert(content);
        } else {
            bookContentMapper.updateById(content);
        }
        return content;
    }

    @Override
    public void removeChapter(Long chapterId) {
        bookContentMapper.deleteById(chapterId);
    }

    @Override
    public void incrHeat(Long bookId) {
        Book b = bookMapper.selectById(bookId);
        if (b == null) return;
        b.setHeat((b.getHeat() == null ? 0 : b.getHeat()) + 1);
        bookMapper.updateById(b);
    }

    @Override
    public Map<String, Object> stats() {
        Map<String, Object> m = new HashMap<>();
        m.put("bookTotal", bookMapper.selectCount(new LambdaQueryWrapper<Book>().isNotNull(Book::getName)));
        m.put("chapterTotal", bookContentMapper.selectCount(null));
        return m;
    }

    private void clearHotCache() {
        try {
            redisTemplate.delete(HOT_CACHE_KEY);
        } catch (Exception ignore) {
        }
    }
}
