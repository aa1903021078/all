package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Reservation;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.entity.SysUser;
import com.dahaiwuliang.mapper.ReservationMapper;
import com.dahaiwuliang.mapper.ShopMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 预约(用户下单 -> 商家确认)
 */
@Service
public class ReservationService {

    private final ReservationMapper reservationMapper;
    private final ShopMapper shopMapper;
    private final UserService userService;

    public ReservationService(ReservationMapper reservationMapper, ShopMapper shopMapper,
                              UserService userService) {
        this.reservationMapper = reservationMapper;
        this.shopMapper = shopMapper;
        this.userService = userService;
    }

    public Reservation create(Reservation reservation) {
        Long userId = UserContext.requireUserId();
        Shop shop = shopMapper.selectById(reservation.getShopId());
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        reservation.setId(null);
        reservation.setUserId(userId);
        reservation.setStatus(0);
        reservationMapper.insert(reservation);
        return reservation;
    }

    public List<Reservation> myReservations() {
        Long userId = UserContext.requireUserId();
        List<Reservation> list = reservationMapper.selectList(new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId).orderByDesc(Reservation::getId));
        fillShop(list);
        return list;
    }

    public void cancel(Long id) {
        Long userId = UserContext.requireUserId();
        Reservation db = reservationMapper.selectById(id);
        if (db == null || !userId.equals(db.getUserId())) {
            throw new BusinessException("预约不存在");
        }
        db.setStatus(4);
        reservationMapper.updateById(db);
    }

    // ---------------- 商家 ----------------

    public List<Reservation> merchantReservations(Integer status) {
        Long merchantId = UserContext.requireUserId();
        List<Long> shopIds = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getMerchantId, merchantId))
                .stream().map(Shop::getId).collect(Collectors.toList());
        if (shopIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<Reservation> wrapper = new LambdaQueryWrapper<Reservation>()
                .in(Reservation::getShopId, shopIds);
        if (status != null) {
            wrapper.eq(Reservation::getStatus, status);
        }
        wrapper.orderByDesc(Reservation::getId);
        List<Reservation> list = reservationMapper.selectList(wrapper);
        fillShop(list);
        fillUser(list);
        return list;
    }

    /** 商家处理预约(确认/拒绝/完成) */
    public void handle(Long id, Integer status) {
        Long merchantId = UserContext.requireUserId();
        Reservation db = reservationMapper.selectById(id);
        if (db == null) {
            throw new BusinessException("预约不存在");
        }
        Shop shop = shopMapper.selectById(db.getShopId());
        if (shop == null || !merchantId.equals(shop.getMerchantId())) {
            throw new BusinessException(403, "只能处理自己店铺的预约");
        }
        db.setStatus(status);
        reservationMapper.updateById(db);
    }

    private void fillShop(List<Reservation> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> shopIds = list.stream().map(Reservation::getShopId)
                .filter(id -> id != null).collect(Collectors.toSet());
        if (shopIds.isEmpty()) {
            return;
        }
        Map<Long, Shop> shopMap = shopMapper.selectBatchIds(shopIds).stream()
                .collect(Collectors.toMap(Shop::getId, s -> s, (a, b) -> a));
        for (Reservation r : list) {
            Shop s = shopMap.get(r.getShopId());
            if (s != null) {
                r.setShopName(s.getName());
                r.setShopCover(s.getCover());
            }
        }
    }

    private void fillUser(List<Reservation> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<Long> userIds = list.stream().map(Reservation::getUserId)
                .filter(id -> id != null).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userService.mapByIds(userIds);
        for (Reservation r : list) {
            SysUser u = userMap.get(r.getUserId());
            if (u != null) {
                r.setUserName(u.getNickname());
            }
        }
    }
}
