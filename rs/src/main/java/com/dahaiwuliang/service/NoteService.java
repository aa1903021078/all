package com.dahaiwuliang.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Note;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.NoteMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 探店笔记(探店消费闭环: 发布带评分笔记 -> 回算店铺综合评分)
 */
@Service
public class NoteService {

    private final NoteMapper noteMapper;
    private final ShopMapper shopMapper;
    private final UserService userService;
    private final LikeService likeService;
    private final ShopService shopService;

    public NoteService(NoteMapper noteMapper, ShopMapper shopMapper, UserService userService,
                       LikeService likeService, ShopService shopService) {
        this.noteMapper = noteMapper;
        this.shopMapper = shopMapper;
        this.userService = userService;
        this.likeService = likeService;
        this.shopService = shopService;
    }

    public Page<Note> pageNotes(long current, long size, Long shopId, String keyword, String sort) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<Note>()
                .eq(Note::getStatus, 1);
        if (shopId != null) {
            wrapper.eq(Note::getShopId, shopId);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getContent, keyword));
        }
        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Note::getLikeCount).orderByDesc(Note::getViewCount);
        } else {
            wrapper.orderByDesc(Note::getId);
        }
        Page<Note> page = noteMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), UserContext.getUserId());
        return page;
    }

    /** 首页笔记流 */
    public List<Note> feed(int limit) {
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getStatus, 1).orderByDesc(Note::getId).last("limit " + limit));
        enrich(notes, UserContext.getUserId());
        return notes;
    }

    public List<Note> listByShop(Long shopId) {
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getShopId, shopId).eq(Note::getStatus, 1).orderByDesc(Note::getId));
        enrich(notes, UserContext.getUserId());
        return notes;
    }

    public List<Note> myNotes() {
        Long userId = UserContext.requireUserId();
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getAuthorId, userId).orderByDesc(Note::getId));
        enrich(notes, userId);
        return notes;
    }

    public Note detail(Long id) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        note.setViewCount((note.getViewCount() == null ? 0 : note.getViewCount()) + 1);
        noteMapper.updateById(note);
        enrich(Collections.singletonList(note), UserContext.getUserId());
        return note;
    }

    @Transactional(rollbackFor = Exception.class)
    public Note saveNote(Note note) {
        Long userId = UserContext.requireUserId();
        note.setId(null);
        note.setAuthorId(userId);
        note.setStatus(1);
        note.setLikeCount(0);
        note.setCommentCount(0);
        note.setViewCount(0);
        if (note.getRecommend() == null) {
            note.setRecommend(0);
        }
        noteMapper.insert(note);
        // 探店闭环: 带评分则回算店铺综合评分
        if (note.getShopId() != null && note.getRating() != null) {
            shopService.recalcRating(note.getShopId());
        }
        return note;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long id) {
        Long userId = UserContext.requireUserId();
        Note db = noteMapper.selectById(id);
        if (db == null) {
            return;
        }
        if (!userId.equals(db.getAuthorId()) && !UserContext.get().isAdmin()) {
            throw new BusinessException(403, "只能删除自己的笔记");
        }
        noteMapper.deleteById(id);
        if (db.getShopId() != null) {
            shopService.recalcRating(db.getShopId());
        }
    }

    /** 后台删除笔记(内容审核), 无需作者校验 */
    @Transactional(rollbackFor = Exception.class)
    public void adminDelete(Long id) {
        Note db = noteMapper.selectById(id);
        if (db == null) {
            return;
        }
        noteMapper.deleteById(id);
        if (db.getShopId() != null) {
            shopService.recalcRating(db.getShopId());
        }
    }

    public boolean like(Long id) {
        return likeService.toggle("NOTE", id);
    }

    // ---------------- 后台 / 审核 ----------------

    public Page<Note> adminPage(long current, long size, String keyword, Integer status) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Note::getTitle, keyword);
        }
        if (status != null) {
            wrapper.eq(Note::getStatus, status);
        }
        wrapper.orderByDesc(Note::getId);
        Page<Note> page = noteMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), null);
        return page;
    }

    @Transactional(rollbackFor = Exception.class)
    public void review(Long id, Integer status) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        note.setStatus(status);
        noteMapper.updateById(note);
        if (note.getShopId() != null) {
            shopService.recalcRating(note.getShopId());
        }
    }

    /** 后台编辑笔记基本信息(标题/正文/图集/评分), 保护统计与作者字段 */
    @Transactional(rollbackFor = Exception.class)
    public Note adminUpdate(Note note) {
        Note db = noteMapper.selectById(note.getId());
        if (db == null) {
            throw new BusinessException("笔记不存在");
        }
        db.setTitle(note.getTitle());
        db.setContent(note.getContent());
        db.setImages(note.getImages());
        if (note.getRating() != null) {
            db.setRating(note.getRating());
        }
        noteMapper.updateById(db);
        if (db.getShopId() != null && db.getRating() != null) {
            shopService.recalcRating(db.getShopId());
        }
        enrich(Collections.singletonList(db), null);
        return db;
    }

    public void setRecommend(Long id, Integer recommend) {
        Note note = noteMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        note.setRecommend(recommend);
        noteMapper.updateById(note);
    }

    /** 回填 作者 / 店铺 / 点赞状态 */
    private void enrich(List<Note> notes, Long userId) {
        if (notes == null || notes.isEmpty()) {
            return;
        }
        Set<Long> authorIds = notes.stream().map(Note::getAuthorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userService.mapByIds(authorIds);

        Set<Long> shopIds = notes.stream().map(Note::getShopId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Shop> shopMap = new HashMap<>();
        if (!shopIds.isEmpty()) {
            shopMapper.selectBatchIds(shopIds).forEach(s -> shopMap.put(s.getId(), s));
        }
        List<Long> ids = notes.stream().map(Note::getId).collect(Collectors.toList());
        Set<Long> likedIds = likeService.likedIds("NOTE", ids, userId);

        for (Note n : notes) {
            SysUser author = userMap.get(n.getAuthorId());
            if (author != null) {
                n.setAuthorName(author.getNickname());
                n.setAuthorAvatar(author.getAvatar());
            }
            Shop shop = shopMap.get(n.getShopId());
            if (shop != null) {
                n.setShopName(shop.getName());
                n.setShopCover(shop.getCover());
            }
            n.setLiked(likedIds.contains(n.getId()));
        }
    }
}
