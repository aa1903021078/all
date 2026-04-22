package com.yuequge.dto;

import com.yuequge.entity.BookComment;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 评论树节点。 */
@Data
public class BookCommentVO {
    private Long id;
    private Long bookId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private Long parentId;
    private String createTime;
    private List<BookCommentVO> children = new ArrayList<>();

    public static BookCommentVO from(BookComment c) {
        BookCommentVO v = new BookCommentVO();
        v.setId(c.getId());
        v.setBookId(c.getBookId());
        v.setUserId(c.getUserId());
        v.setContent(c.getContent());
        v.setParentId(c.getParentId());
        v.setCreateTime(c.getCreateTime() == null ? null : c.getCreateTime().toString());
        return v;
    }
}
