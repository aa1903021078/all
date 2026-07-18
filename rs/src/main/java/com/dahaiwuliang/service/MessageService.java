package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.dto.MessageVO;
import com.dahaiwuliang.entity.Comment;
import com.dahaiwuliang.entity.Note;
import com.dahaiwuliang.entity.Recipe;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.entity.UserLike;
import com.dahaiwuliang.mapper.CommentMapper;
import com.dahaiwuliang.mapper.NoteMapper;
import com.dahaiwuliang.mapper.RecipeMapper;
import com.dahaiwuliang.mapper.UserLikeMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 我的消息: 汇总"收到的点赞 / 收到的评论回复", 数据来源于既有的 user_like / comment 表.
 */
@Service
public class MessageService {

    private final UserLikeMapper likeMapper;
    private final CommentMapper commentMapper;
    private final NoteMapper noteMapper;
    private final RecipeMapper recipeMapper;
    private final UserService userService;

    public MessageService(UserLikeMapper likeMapper, CommentMapper commentMapper,
                          NoteMapper noteMapper, RecipeMapper recipeMapper, UserService userService) {
        this.likeMapper = likeMapper;
        this.commentMapper = commentMapper;
        this.noteMapper = noteMapper;
        this.recipeMapper = recipeMapper;
        this.userService = userService;
    }

    /** 我的全部消息, 分为 likes / comments 两组 */
    public Map<String, Object> myMessages() {
        Long myId = UserContext.requireUserId();

        // 我发布的笔记 / 菜谱(标题用于展示)
        Map<Long, String> noteTitles = new HashMap<>();
        List<Note> myNotes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getAuthorId, myId).select(Note::getId, Note::getTitle));
        for (Note n : myNotes) {
            noteTitles.put(n.getId(), n.getTitle());
        }
        Map<Long, String> recipeTitles = new HashMap<>();
        List<Recipe> myRecipes = recipeMapper.selectList(new LambdaQueryWrapper<Recipe>()
                .eq(Recipe::getAuthorId, myId).select(Recipe::getId, Recipe::getTitle));
        for (Recipe r : myRecipes) {
            recipeTitles.put(r.getId(), r.getTitle());
        }

        Set<Long> noteIds = noteTitles.keySet();
        Set<Long> recipeIds = recipeTitles.keySet();

        List<MessageVO> likes = buildLikes(myId, noteIds, recipeIds, noteTitles, recipeTitles);
        List<MessageVO> comments = buildComments(myId, noteIds, recipeIds, noteTitles, recipeTitles);

        Map<String, Object> result = new HashMap<>();
        result.put("likes", likes);
        result.put("comments", comments);
        result.put("likeCount", likes.size());
        result.put("commentCount", comments.size());
        return result;
    }

    /** 收到的点赞: 别人点赞了我的笔记 / 菜谱 */
    private List<MessageVO> buildLikes(Long myId, Set<Long> noteIds, Set<Long> recipeIds,
                                       Map<Long, String> noteTitles, Map<Long, String> recipeTitles) {
        List<UserLike> all = new ArrayList<>();
        if (!noteIds.isEmpty()) {
            all.addAll(likeMapper.selectList(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getTargetType, "NOTE")
                    .in(UserLike::getTargetId, noteIds)
                    .ne(UserLike::getUserId, myId)));
        }
        if (!recipeIds.isEmpty()) {
            all.addAll(likeMapper.selectList(new LambdaQueryWrapper<UserLike>()
                    .eq(UserLike::getTargetType, "RECIPE")
                    .in(UserLike::getTargetId, recipeIds)
                    .ne(UserLike::getUserId, myId)));
        }
        Map<Long, SysUser> userMap = loadUsers(collectUserIds(all, null));
        List<MessageVO> list = new ArrayList<>();
        for (UserLike like : all) {
            MessageVO vo = new MessageVO();
            vo.setId(like.getId());
            vo.setKind("LIKE");
            vo.setFromUserId(like.getUserId());
            fillFromUser(vo, userMap.get(like.getUserId()));
            vo.setTargetType(like.getTargetType());
            vo.setTargetId(like.getTargetId());
            vo.setTargetTitle(titleOf(like.getTargetType(), like.getTargetId(), noteTitles, recipeTitles));
            vo.setCreateTime(like.getCreateTime());
            list.add(vo);
        }
        sortByTimeDesc(list);
        return limit(list, 100);
    }

    /** 收到的评论/回复: 别人评论了我的内容, 或直接回复了我 */
    private List<MessageVO> buildComments(Long myId, Set<Long> noteIds, Set<Long> recipeIds,
                                          Map<Long, String> noteTitles, Map<Long, String> recipeTitles) {
        Map<Long, Comment> merged = new HashMap<>();
        if (!noteIds.isEmpty()) {
            for (Comment c : commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getTargetType, "NOTE")
                    .in(Comment::getTargetId, noteIds)
                    .ne(Comment::getUserId, myId))) {
                merged.put(c.getId(), c);
            }
        }
        if (!recipeIds.isEmpty()) {
            for (Comment c : commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getTargetType, "RECIPE")
                    .in(Comment::getTargetId, recipeIds)
                    .ne(Comment::getUserId, myId))) {
                merged.put(c.getId(), c);
            }
        }
        // 直接回复我的评论(可能不在我的内容下)
        for (Comment c : commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getReplyUserId, myId)
                .ne(Comment::getUserId, myId))) {
            merged.put(c.getId(), c);
        }

        List<Comment> comments = new ArrayList<>(merged.values());
        Map<Long, SysUser> userMap = loadUsers(collectUserIds(null, comments));
        List<MessageVO> list = new ArrayList<>();
        for (Comment c : comments) {
            MessageVO vo = new MessageVO();
            vo.setId(c.getId());
            boolean reply = myId.equals(c.getReplyUserId());
            vo.setKind(reply ? "REPLY" : "COMMENT");
            vo.setFromUserId(c.getUserId());
            fillFromUser(vo, userMap.get(c.getUserId()));
            vo.setTargetType(c.getTargetType());
            vo.setTargetId(c.getTargetId());
            vo.setTargetTitle(titleOf(c.getTargetType(), c.getTargetId(), noteTitles, recipeTitles));
            vo.setContent(c.getContent());
            vo.setCreateTime(c.getCreateTime());
            list.add(vo);
        }
        sortByTimeDesc(list);
        return limit(list, 100);
    }

    private String titleOf(String type, Long id, Map<Long, String> noteTitles, Map<Long, String> recipeTitles) {
        if ("NOTE".equals(type)) {
            return noteTitles.get(id);
        }
        if ("RECIPE".equals(type)) {
            return recipeTitles.get(id);
        }
        return null;
    }

    private Set<Long> collectUserIds(List<UserLike> likes, List<Comment> comments) {
        Set<Long> ids = new HashSet<>();
        if (likes != null) {
            for (UserLike l : likes) {
                ids.add(l.getUserId());
            }
        }
        if (comments != null) {
            for (Comment c : comments) {
                ids.add(c.getUserId());
            }
        }
        return ids;
    }

    private Map<Long, SysUser> loadUsers(Set<Long> ids) {
        return userService.mapByIds(ids);
    }

    private void fillFromUser(MessageVO vo, SysUser u) {
        if (u != null) {
            vo.setFromUserName(u.getNickname());
            vo.setFromUserAvatar(u.getAvatar());
        }
    }

    private void sortByTimeDesc(List<MessageVO> list) {
        list.sort(Comparator.comparing(MessageVO::getCreateTime,
                Comparator.nullsLast(Comparator.naturalOrder())).reversed());
    }

    private List<MessageVO> limit(List<MessageVO> list, int max) {
        if (list.size() <= max) {
            return list;
        }
        return new ArrayList<>(list.subList(0, max));
    }
}
