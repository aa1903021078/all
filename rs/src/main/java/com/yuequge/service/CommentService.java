package com.yuequge.service;

import com.yuequge.common.PageResult;
import com.yuequge.dto.BookCommentVO;
import com.yuequge.entity.BookComment;

import java.util.List;

public interface CommentService {
    /** 获取图书评论（楼中楼树）。 */
    List<BookCommentVO> listByBook(Long bookId);

    BookComment create(Long bookId, Long userId, String content, Long parentId);

    void delete(Long id, Long userId, boolean admin);

    PageResult<BookComment> pageAll(long page, long size);
}
