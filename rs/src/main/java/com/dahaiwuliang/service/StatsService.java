package com.dahaiwuliang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.Reservation;
import com.dahaiwuliang.entity.Shop;
import com.dahaiwuliang.mapper.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计(平台大屏 + 商家看板)
 */
@Service
public class StatsService {

    private final ShopMapper shopMapper;
    private final RecipeMapper recipeMapper;
    private final NoteMapper noteMapper;
    private final SysUserMapper userMapper;
    private final ShopCheckinMapper checkinMapper;
    private final ReservationMapper reservationMapper;

    public StatsService(ShopMapper shopMapper, RecipeMapper recipeMapper, NoteMapper noteMapper,
                        SysUserMapper userMapper, ShopCheckinMapper checkinMapper,
                        ReservationMapper reservationMapper) {
        this.shopMapper = shopMapper;
        this.recipeMapper = recipeMapper;
        this.noteMapper = noteMapper;
        this.userMapper = userMapper;
        this.checkinMapper = checkinMapper;
        this.reservationMapper = reservationMapper;
    }

    /** 平台概览指标 */
    public Map<String, Object> overview() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("shopCount", shopMapper.selectCount(null));
        map.put("recipeCount", recipeMapper.selectCount(null));
        map.put("noteCount", noteMapper.selectCount(null));
        map.put("userCount", userMapper.selectCount(null));
        map.put("checkinCount", checkinMapper.selectCount(null));
        map.put("reservationCount", reservationMapper.selectCount(null));
        return map;
    }

    public List<Map<String, Object>> categoryDistribution() {
        return shopMapper.categoryDistribution();
    }

    public List<Map<String, Object>> priceDistribution() {
        return shopMapper.priceDistribution();
    }

    public List<Map<String, Object>> checkinTrend(Long shopId) {
        return checkinMapper.checkinTrend(shopId);
    }

    public List<Map<String, Object>> publishTrend() {
        return noteMapper.publishTrend();
    }

    /** 大屏聚合数据 */
    public Map<String, Object> dashboard() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("overview", overview());
        map.put("categoryDistribution", categoryDistribution());
        map.put("priceDistribution", priceDistribution());
        map.put("checkinTrend", checkinTrend(null));
        map.put("publishTrend", publishTrend());
        return map;
    }

    /** 商家看板 */
    public Map<String, Object> merchantOverview() {
        Long merchantId = UserContext.requireUserId();
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId));
        int totalView = 0;
        int totalCheckin = 0;
        double ratingSum = 0;
        int ratingShops = 0;
        for (Shop s : shops) {
            totalView += s.getViewCount() == null ? 0 : s.getViewCount();
            totalCheckin += s.getCheckinCount() == null ? 0 : s.getCheckinCount();
            if (s.getRating() != null && s.getRating().doubleValue() > 0) {
                ratingSum += s.getRating().doubleValue();
                ratingShops++;
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("shopCount", shops.size());
        map.put("totalView", totalView);
        map.put("totalCheckin", totalCheckin);
        map.put("avgRating", ratingShops == 0 ? 0 : Math.round(ratingSum / ratingShops * 10) / 10.0);

        List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
        if (shopIds.isEmpty()) {
            map.put("reservationCount", 0);
            map.put("pendingReservation", 0);
        } else {
            Long total = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                    .in(Reservation::getShopId, shopIds));
            Long pending = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>()
                    .in(Reservation::getShopId, shopIds).eq(Reservation::getStatus, 0));
            map.put("reservationCount", total);
            map.put("pendingReservation", pending);
        }
        map.put("shops", shops);
        return map;
    }

    /** 商家某店铺点亮趋势(校验归属简化: 直接查该店铺) */
    public List<Map<String, Object>> merchantCheckinTrend(Long shopId) {
        if (shopId == null) {
            return Collections.emptyList();
        }
        return checkinMapper.checkinTrend(shopId);
    }
}
