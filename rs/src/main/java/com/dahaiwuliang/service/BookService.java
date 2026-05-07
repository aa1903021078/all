package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dahaiwuliang.entity.Book;

public interface BookService extends IService<Book> {
    IPage<Book> searchBooks(int page, int size, Long categoryId, String keyword, String grade, String major);
}
