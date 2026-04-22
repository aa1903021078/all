package com.yuequge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuequge.common.PageResult;
import com.yuequge.dto.BookCommentVO;
import com.yuequge.entity.BookComment;
import com.yuequge.entity.User;
import com.yuequge.exception.BizException;
import com.yuequge.mapper.BookCommentMapper;
import com.yuequge.mapper.UserMapper;
import com.yuequge.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final BookCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public List<BookCommentVO> listByBook(Long bookId) {
        List<BookComment> all = commentMapper.selectList(new LambdaQueryWrapper<BookComment>()
                .eq(BookComment::getBookId, bookId)
                .orderByAsc(BookComment::getCreateTime));
        if (all.isEmpty()) return Collections.emptyList();

        // 一次性拉用户，避免 N+1
        Set<Long> userIds = all.stream().map(BookComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        Map<Long, BookCommentVO> voMap = new LinkedHashMap<>();
        for (BookComment c : all) {
            BookCommentVO vo = BookCommentVO.from(c);
            User u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setUsername(u.getUsername());
                vo.setAvatar(u.getAvatar());
            }
            voMap.put(c.getId(), vo);
        }
        List<BookCommentVO> roots = new ArrayList<>();
        for (BookCommentVO vo : voMap.values()) {
            if (vo.getParentId() == null) {
                roots.add(vo);
            } else {
                BookCommentVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    // 父节点被删，作为根显示
                    roots.add(vo);
                }
            }
        }
        return roots;
    }

    @Override
    public BookComment create(Long bookId, Long userId, String content, Long parentId) {
        if (content == null || content.isBlank()) throw new BizException("评论内容不能为空");
        BookComment c = new BookComment();
        c.setBookId(bookId);
        c.setUserId(userId);
        c.setContent(content);
        c.setParentId(parentId);
        c.setCreateTime(LocalDateTime.now());
        commentMapper.insert(c);
        return c;
    }

    @Override
    public void delete(Long id, Long userId, boolean admin) {
        BookComment c = commentMapper.selectById(id);
        if (c == null) return;
        if (!admin && !Objects.equals(c.getUserId(), userId)) {
            throw new BizException(403, "无权删除他人评论");
        }
        commentMapper.deleteById(id);
    }

    @Override
    public PageResult<BookComment> pageAll(long page, long size) {
        Page<BookComment> p = commentMapper.selectPage(Page.of(page, size),
                new LambdaQueryWrapper<BookComment>().orderByDesc(BookComment::getCreateTime));
        return PageResult.of(p.getTotal(), p.getRecords());
    }
}
