package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通用点赞(NOTE / RECIPE / COMMENT / REPOST)
 */
@Service
public class LikeService {

    private final UserLikeMapper likeMapper;
    private final NoteMapper noteMapper;
    private final RecipeMapper recipeMapper;
    private final CommentMapper commentMapper;
    private final RecipeRepostMapper repostMapper;

    public LikeService(UserLikeMapper likeMapper, NoteMapper noteMapper, RecipeMapper recipeMapper,
                       CommentMapper commentMapper, RecipeRepostMapper repostMapper) {
        this.likeMapper = likeMapper;
        this.noteMapper = noteMapper;
        this.recipeMapper = recipeMapper;
        this.commentMapper = commentMapper;
        this.repostMapper = repostMapper;
    }

    /** 点赞/取消, 返回点赞后的状态 */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(String targetType, Long targetId) {
        Long userId = UserContext.requireUserId();
        UserLike exist = likeMapper.selectOne(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetType)
                .eq(UserLike::getTargetId, targetId));
        if (exist != null) {
            likeMapper.deleteById(exist.getId());
            updateCount(targetType, targetId, -1);
            return false;
        }
        UserLike like = new UserLike();
        like.setUserId(userId);
        like.setTargetType(targetType);
        like.setTargetId(targetId);
        like.setCreateTime(LocalDateTime.now());
        likeMapper.insert(like);
        updateCount(targetType, targetId, 1);
        return true;
    }

    private void updateCount(String targetType, Long targetId, int delta) {
        if ("NOTE".equals(targetType)) {
            Note n = noteMapper.selectById(targetId);
            if (n != null) {
                n.setLikeCount(Math.max(0, nz(n.getLikeCount()) + delta));
                noteMapper.updateById(n);
            }
        } else if ("RECIPE".equals(targetType)) {
            Recipe r = recipeMapper.selectById(targetId);
            if (r != null) {
                r.setLikeCount(Math.max(0, nz(r.getLikeCount()) + delta));
                recipeMapper.updateById(r);
            }
        } else if ("COMMENT".equals(targetType)) {
            Comment c = commentMapper.selectById(targetId);
            if (c != null) {
                c.setLikeCount(Math.max(0, nz(c.getLikeCount()) + delta));
                commentMapper.updateById(c);
            }
        } else if ("REPOST".equals(targetType)) {
            RecipeRepost rp = repostMapper.selectById(targetId);
            if (rp != null) {
                rp.setLikeCount(Math.max(0, nz(rp.getLikeCount()) + delta));
                repostMapper.updateById(rp);
            }
        }
    }

    /** 当前用户是否点赞了某目标 */
    public boolean isLiked(String targetType, Long targetId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = likeMapper.selectCount(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetType)
                .eq(UserLike::getTargetId, targetId));
        return count != null && count > 0;
    }

    /** 批量: 当前用户在这批目标里点赞过的 id 集合 */
    public Set<Long> likedIds(String targetType, Collection<Long> targetIds, Long userId) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return Collections.emptySet();
        }
        List<UserLike> list = likeMapper.selectList(new LambdaQueryWrapper<UserLike>()
                .eq(UserLike::getUserId, userId)
                .eq(UserLike::getTargetType, targetType)
                .in(UserLike::getTargetId, new HashSet<>(targetIds)));
        return list.stream().map(UserLike::getTargetId).collect(Collectors.toSet());
    }

    private int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
