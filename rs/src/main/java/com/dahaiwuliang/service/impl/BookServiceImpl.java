package com.dahaiwuliang.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dahaiwuliang.entity.Book;
import com.dahaiwuliang.mapper.BookMapper;
import com.dahaiwuliang.service.BookService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Override
    public IPage<Book> searchBooks(int page, int size, Long categoryId, String keyword, String grade, String major) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, 1); // 只查上架的
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Book::getTitle, keyword)
                    .or().like(Book::getAuthor, keyword)
                    .or().like(Book::getCourseName, keyword));
        }
        if (StringUtils.hasText(grade)) {
            wrapper.eq(Book::getGrade, grade);
        }
        if (StringUtils.hasText(major)) {
            wrapper.like(Book::getMajor, major);
        }
        wrapper.orderByDesc(Book::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }
}
