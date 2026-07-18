package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Comment;
import com.dahaiwuliang.entity.Note;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.CommentMapper;
import com.dahaiwuliang.mapper.NoteMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 评论(两级: 顶级评论 + 回复), 支持 NOTE / RECIPE
 */
@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final UserService userService;
    private final LikeService likeService;

    public CommentService(CommentMapper commentMapper, NoteMapper noteMapper,
                          UserService userService, LikeService likeService) {
        this.commentMapper = commentMapper;
        this.noteMapper = noteMapper;
        this.userService = userService;
        this.likeService = likeService;
    }

    /** 评论树 */
    public List<Comment> listTree(String targetType, Long targetId) {
        List<Comment> all = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getTargetType, targetType)
                .eq(Comment::getTargetId, targetId)
                .orderByAsc(Comment::getId));
        if (all.isEmpty()) {
            return new ArrayList<>();
        }
        // 回填用户信息 + 点赞状态
        Set<Long> userIds = new HashSet<>();
        for (Comment c : all) {
            userIds.add(c.getUserId());
            if (c.getReplyUserId() != null) {
                userIds.add(c.getReplyUserId());
            }
        }
        Map<Long, SysUser> userMap = userService.mapByIds(userIds);
        List<Long> ids = all.stream().map(Comment::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.likedIds("COMMENT", ids, UserContext.getUserId());
        for (Comment c : all) {
            SysUser u = userMap.get(c.getUserId());
            if (u != null) {
                c.setUserName(u.getNickname());
                c.setUserAvatar(u.getAvatar());
            }
            if (c.getReplyUserId() != null) {
                SysUser ru = userMap.get(c.getReplyUserId());
                c.setReplyUserName(ru == null ? null : ru.getNickname());
            }
            c.setLiked(likedIds.contains(c.getId()));
        }
        // 组装两级
        List<Comment> tops = new ArrayList<>();
        for (Comment c : all) {
            if (c.getParentId() == null || c.getParentId() == 0L) {
                c.setChildren(new ArrayList<>());
                tops.add(c);
            }
        }
        for (Comment c : all) {
            if (c.getParentId() != null && c.getParentId() != 0L) {
                for (Comment top : tops) {
                    if (top.getId().equals(c.getParentId())) {
                        top.getChildren().add(c);
                        break;
                    }
                }
            }
        }
        tops.sort((a, b) -> Long.compare(b.getId(), a.getId()));
        return tops;
    }

    @Transactional(rollbackFor = Exception.class)
    public Comment add(Comment comment) {
        Long userId = UserContext.requireUserId();
        comment.setId(null);
        comment.setUserId(userId);
        comment.setLikeCount(0);
        if (comment.getParentId() == null) {
            comment.setParentId(0L);
        }
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
        // 笔记评论数 +1
        if ("NOTE".equals(comment.getTargetType())) {
            Note note = noteMapper.selectById(comment.getTargetId());
            if (note != null) {
                note.setCommentCount((note.getCommentCount() == null ? 0 : note.getCommentCount()) + 1);
                noteMapper.updateById(note);
            }
        }
        // 回填当前用户信息用于即时渲染
        SysUser u = userService.getById(userId);
        if (u != null) {
            comment.setUserName(u.getNickname());
            comment.setUserAvatar(u.getAvatar());
        }
        comment.setChildren(new ArrayList<>());
        return comment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long userId = UserContext.requireUserId();
        Comment db = commentMapper.selectById(id);
        if (db == null) {
            return;
        }
        boolean admin = UserContext.get() != null && UserContext.get().isAdmin();
        if (!userId.equals(db.getUserId()) && !admin) {
            throw new BusinessException(403, "只能删除自己的评论");
        }
        commentMapper.deleteById(id);
        // 顶级评论: 连同其回复一并删除
        int removed = 1;
        if (db.getParentId() == null || db.getParentId() == 0L) {
            List<Comment> children = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getParentId, id));
            if (!children.isEmpty()) {
                commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, id));
                removed += children.size();
            }
        }
        if ("NOTE".equals(db.getTargetType())) {
            Note note = noteMapper.selectById(db.getTargetId());
            if (note != null) {
                int c = (note.getCommentCount() == null ? 0 : note.getCommentCount()) - removed;
                note.setCommentCount(Math.max(0, c));
                noteMapper.updateById(note);
            }
        }
    }

    public boolean like(Long id) {
        return likeService.toggle("COMMENT", id);
    }
}
