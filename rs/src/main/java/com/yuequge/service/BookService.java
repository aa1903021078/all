package com.yuequge.service;

import com.yuequge.common.PageResult;
import com.yuequge.entity.Book;
import com.yuequge.entity.BookContent;

import java.util.List;
import java.util.Map;

public interface BookService {
    PageResult<Book> page(long page, long size, String keyword, String type);

    Book getById(Long id);

    List<BookContent> listChapters(Long bookId);

    BookContent getChapter(Long chapterId);

    /** 首页热门榜单（Redis 缓存）。 */
    List<Book> hotList(int limit);

    Book save(Book book);

    void remove(Long id);

    BookContent saveChapter(BookContent content);

    void removeChapter(Long chapterId);

    /** 阅读计数 +1（用于热度）。 */
    void incrHeat(Long bookId);

    /** 统计信息：书籍数/章节数/按类型聚合。 */
    Map<String, Object> stats();
}
