package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Note;
import com.dahaiwuliang.entity.NoteAppeal;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.NoteAppealMapper;
import com.dahaiwuliang.mapper.NoteMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 差评申诉: 商家提交, 平台(审核员/管理员)处理
 */
@Service
public class AppealService {

    private final NoteAppealMapper appealMapper;
    private final NoteMapper noteMapper;
    private final ShopMapper shopMapper;
    private final UserService userService;

    public AppealService(NoteAppealMapper appealMapper, NoteMapper noteMapper,
                         ShopMapper shopMapper, UserService userService) {
        this.appealMapper = appealMapper;
        this.noteMapper = noteMapper;
        this.shopMapper = shopMapper;
        this.userService = userService;
    }

    /** 商家提交申诉 */
    @Transactional(rollbackFor = Exception.class)
    public NoteAppeal submit(NoteAppeal appeal) {
        Long merchantId = UserContext.requireUserId();
        if (appeal.getNoteId() == null) {
            throw new BusinessException("请选择要申诉的笔记");
        }
        Note note = noteMapper.selectById(appeal.getNoteId());
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        Shop shop = shopMapper.selectById(note.getShopId());
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(403, "只能对自己店铺的评价申诉");
        }
        appeal.setId(null);
        appeal.setMerchantId(merchantId);
        appeal.setShopId(shop.getId());
        appeal.setStatus(0);
        appeal.setReply(null);
        appealMapper.insert(appeal);
        return appeal;
    }

    /** 商家查看自己的申诉 */
    public List<NoteAppeal> myAppeals() {
        Long merchantId = UserContext.requireUserId();
        List<NoteAppeal> list = appealMapper.selectList(new LambdaQueryWrapper<NoteAppeal>()
                .eq(NoteAppeal::getMerchantId, merchantId)
                .orderByDesc(NoteAppeal::getId));
        fillNote(list, false);
        return list;
    }

    /** 平台查看全部申诉 */
    public List<NoteAppeal> adminList(Integer status) {
        LambdaQueryWrapper<NoteAppeal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(NoteAppeal::getStatus, status);
        }
        wrapper.orderByDesc(NoteAppeal::getId);
        List<NoteAppeal> list = appealMapper.selectList(wrapper);
        fillNote(list, true);
        return list;
    }

    /** 平台处理申诉(1已受理 2已驳回), 可附回复 */
    public void handle(Long id, Integer status, String reply) {
        NoteAppeal db = appealMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("申诉不存在");
        }
        db.setStatus(status);
        db.setReply(reply);
        appealMapper.updateById(db);
    }

    private void fillNote(List<NoteAppeal> list, boolean withMerchant) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> noteIds = list.stream().map(NoteAppeal::getNoteId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, Note> noteMap = noteIds.isEmpty() ? java.util.Collections.emptyMap()
                : noteMapper.selectBatchIds(noteIds).stream()
                .collect(Collectors.toMap(Note::getId, n -> n, (a, b) -> a));
        Set<Long> shopIds = list.stream().map(NoteAppeal::getShopId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, Shop> shopMap = shopIds.isEmpty() ? java.util.Collections.emptyMap()
                : shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s, (a, b) -> a));
        Map<Long, SysUser> userMap = java.util.Collections.emptyMap();
        if (withMerchant) {
            Set<Long> merchantIds = list.stream().map(NoteAppeal::getMerchantId)
                    .filter(id -> id != null).collect(Collectors.toSet());
            userMap = userService.mapByIds(merchantIds);
        }
        for (NoteAppeal a : list) {
            Note n = noteMap.get(a.getNoteId());
            if (n != null) {
                a.setNoteTitle(n.getTitle());
                a.setNoteContent(n.getContent());
                a.setNoteRating(n.getRating());
            }
            Shop s = shopMap.get(a.getShopId());
            if (s != null) {
                a.setShopName(s.getName());
            }
            if (withMerchant) {
                SysUser u = userMap.get(a.getMerchantId());
                if (u != null) {
                    a.setMerchantName(u.getNickname());
                }
            }
        }
    }
}
