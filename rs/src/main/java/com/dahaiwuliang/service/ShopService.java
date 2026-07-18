package com.dahaiwuliang.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dahaiwuliang.common.BusinessException;
import com.dahaiwuliang.common.UserContext;
import com.dahaiwuliang.entity.*;
import com.dahaiwuliang.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 探店 / 店铺
 */
@Service
public class ShopService {

    private final ShopMapper shopMapper;
    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final ShopCheckinMapper checkinMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final NoteMapper noteMapper;

    public ShopService(ShopMapper shopMapper, DishMapper dishMapper, CategoryMapper categoryMapper,
                       ShopCheckinMapper checkinMapper, UserFavoriteMapper favoriteMapper, NoteMapper noteMapper) {
        this.shopMapper = shopMapper;
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.checkinMapper = checkinMapper;
        this.favoriteMapper = favoriteMapper;
        this.noteMapper = noteMapper;
    }

    /** 分页查询上架店铺 */
    public Page<Shop> pageShops(long current, long size, String keyword, Long categoryId, String sort) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Shop::getName, keyword).or().like(Shop::getAddress, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Shop::getCategoryId, categoryId);
        }
        applySort(wrapper, sort);
        Page<Shop> page = shopMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), UserContext.getUserId());
        return page;
    }

    private void applySort(LambdaQueryWrapper<Shop> wrapper, String sort) {
        if (sort == null) {
            sort = "";
        }
        switch (sort) {
            case "rating":
                wrapper.orderByDesc(Shop::getRating);
                break;
            case "priceAsc":
                wrapper.orderByAsc(Shop::getAvgPrice);
                break;
            case "priceDesc":
                wrapper.orderByDesc(Shop::getAvgPrice);
                break;
            case "checkin":
                wrapper.orderByDesc(Shop::getCheckinCount);
                break;
            default:
                wrapper.orderByDesc(Shop::getId);
        }
    }

    /** 店铺详情 */
    public Shop detail(Long id) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setViewCount((shop.getViewCount() == null ? 0 : shop.getViewCount()) + 1);
        shopMapper.updateById(shop);
        enrich(Collections.singletonList(shop), UserContext.getUserId());
        shop.setDishes(dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, id).orderByAsc(Dish::getSort)));
        return shop;
    }

    /** 地图点位: 所有上架店铺(含坐标) */
    public List<Shop> mapShops(Long categoryId, Boolean onlyLit) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .isNotNull(Shop::getLongitude);
        if (categoryId != null) {
            wrapper.eq(Shop::getCategoryId, categoryId);
        }
        List<Shop> shops = shopMapper.selectList(wrapper);
        enrich(shops, UserContext.getUserId());
        if (Boolean.TRUE.equals(onlyLit)) {
            shops = shops.stream().filter(s -> Boolean.TRUE.equals(s.getLit())).collect(Collectors.toList());
        }
        return shops;
    }

    /** 周边美食雷达 */
    public List<Shop> nearby(double lng, double lat, double radiusKm, Long categoryId,
                             BigDecimal minRating, Boolean onlyLit) {
        List<Shop> shops = mapShops(categoryId, onlyLit);
        List<Shop> result = new ArrayList<>();
        for (Shop s : shops) {
            if (s.getLongitude() == null || s.getLatitude() == null) {
                continue;
            }
            if (minRating != null && (s.getRating() == null || s.getRating().compareTo(minRating) < 0)) {
                continue;
            }
            double distance = distanceKm(lat, lng, s.getLatitude().doubleValue(), s.getLongitude().doubleValue());
            if (distance <= radiusKm) {
                // 借用 viewCount 之外无字段, 通过 map 排序; 这里直接排序结果
                result.add(s);
            }
        }
        result.sort(Comparator.comparingDouble(s ->
                distanceKm(lat, lng, s.getLatitude().doubleValue(), s.getLongitude().doubleValue())));
        return result;
    }

    private double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double r = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /** 首页推荐店铺 */
    public List<Shop> recommend(int limit) {
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1).eq(Shop::getRecommend, 1)
                .orderByDesc(Shop::getRating).last("limit " + limit));
        enrich(shops, UserContext.getUserId());
        return shops;
    }

    /** 高分店铺榜单 */
    public List<Shop> ranking(int limit) {
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getStatus, 1)
                .orderByDesc(Shop::getRating).orderByDesc(Shop::getCheckinCount)
                .last("limit " + limit));
        enrich(shops, UserContext.getUserId());
        return shops;
    }

    /** 点亮店铺 */
    @Transactional(rollbackFor = Exception.class)
    public boolean checkin(Long shopId) {
        Long userId = UserContext.requireUserId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        Long exist = checkinMapper.selectCount(new LambdaQueryWrapper<ShopCheckin>()
                .eq(ShopCheckin::getUserId, userId).eq(ShopCheckin::getShopId, shopId));
        if (exist != null && exist > 0) {
            throw new BusinessException("你已点亮过该店铺");
        }
        ShopCheckin checkin = new ShopCheckin();
        checkin.setUserId(userId);
        checkin.setShopId(shopId);
        checkin.setNoteId(0L);
        checkin.setCheckinTime(LocalDateTime.now());
        checkinMapper.insert(checkin);
        shop.setCheckinCount((shop.getCheckinCount() == null ? 0 : shop.getCheckinCount()) + 1);
        shopMapper.updateById(shop);
        return true;
    }

    /** 我点亮的店铺 */
    public List<Shop> myCheckinShops() {
        Long userId = UserContext.requireUserId();
        List<ShopCheckin> checkins = checkinMapper.selectList(new LambdaQueryWrapper<ShopCheckin>()
                .eq(ShopCheckin::getUserId, userId).orderByDesc(ShopCheckin::getCheckinTime));
        if (checkins.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> shopIds = checkins.stream().map(ShopCheckin::getShopId).collect(Collectors.toList());
        List<Shop> shops = shopMapper.selectBatchIds(shopIds);
        enrich(shops, userId);
        return shops;
    }

    // ---------------- 商家 / 后台 管理 ----------------

    @Transactional(rollbackFor = Exception.class)
    public Shop saveShop(Shop shop, Long merchantId) {
        if (merchantId != null) {
            shop.setMerchantId(merchantId);
        }
        if (shop.getStatus() == null) {
            shop.setStatus(1);
        }
        shop.setRating(BigDecimal.ZERO);
        shop.setRatingCount(0);
        shop.setViewCount(0);
        shop.setCheckinCount(0);
        shopMapper.insert(shop);
        return shop;
    }

    @Transactional(rollbackFor = Exception.class)
    public Shop updateShop(Shop shop, Long operatorMerchantId) {
        Shop db = shopMapper.selectById(shop.getId());
        if (db == null) {
            throw new BusinessException("店铺不存在");
        }
        if (operatorMerchantId != null && !operatorMerchantId.equals(db.getMerchantId())) {
            throw new BusinessException(403, "只能修改自己的店铺");
        }
        // 保护统计字段不被覆盖
        shop.setRating(null);
        shop.setRatingCount(null);
        shop.setViewCount(null);
        shop.setCheckinCount(null);
        shop.setMerchantId(null);
        shopMapper.updateById(shop);
        return shopMapper.selectById(shop.getId());
    }

    public void deleteShop(Long id) {
        shopMapper.deleteById(id);
    }

    public void changeStatus(Long id, Integer status) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setStatus(status);
        shopMapper.updateById(shop);
    }

    public void setRecommend(Long id, Integer recommend) {
        Shop shop = shopMapper.selectById(id);
        if (shop == null) {
            throw new BusinessException("店铺不存在");
        }
        shop.setRecommend(recommend);
        shopMapper.updateById(shop);
    }

    // ---------------- 菜品管理 ----------------

    public List<Dish> listDishes(Long shopId) {
        return dishMapper.selectList(new LambdaQueryWrapper<Dish>()
                .eq(Dish::getShopId, shopId).orderByAsc(Dish::getSort));
    }

    @Transactional(rollbackFor = Exception.class)
    public Dish saveDish(Dish dish) {
        dishMapper.insert(dish);
        return dish;
    }

    public Dish updateDish(Dish dish) {
        dishMapper.updateById(dish);
        return dishMapper.selectById(dish.getId());
    }

    public void deleteDish(Long id) {
        dishMapper.deleteById(id);
    }

    public List<Shop> merchantShops(Long merchantId) {
        List<Shop> shops = shopMapper.selectList(new LambdaQueryWrapper<Shop>()
                .eq(Shop::getMerchantId, merchantId).orderByDesc(Shop::getId));
        enrich(shops, null);
        return shops;
    }

    public Page<Shop> adminPage(long current, long size, String keyword, Long categoryId, Integer status) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Shop::getName, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Shop::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Shop::getStatus, status);
        }
        wrapper.orderByDesc(Shop::getId);
        Page<Shop> page = shopMapper.selectPage(new Page<>(current, size), wrapper);
        enrich(page.getRecords(), null);
        return page;
    }

    /** 根据笔记评分重新计算店铺综合评分 */
    public void recalcRating(Long shopId) {
        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getShopId, shopId).eq(Note::getStatus, 1).isNotNull(Note::getRating));
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            return;
        }
        if (notes.isEmpty()) {
            shop.setRating(BigDecimal.ZERO);
            shop.setRatingCount(0);
        } else {
            double avg = notes.stream().mapToInt(Note::getRating).average().orElse(0);
            shop.setRating(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
            shop.setRatingCount(notes.size());
        }
        shopMapper.updateById(shop);
    }

    /** 回填 菜系名 / 收藏 / 点亮 状态 */
    private void enrich(List<Shop> shops, Long userId) {
        if (shops == null || shops.isEmpty()) {
            return;
        }
        Set<Long> categoryIds = shops.stream().map(Shop::getCategoryId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> categoryNames = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            categoryMapper.selectBatchIds(categoryIds)
                    .forEach(c -> categoryNames.put(c.getId(), c.getName()));
        }
        List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
        Set<Long> favoritedIds = new HashSet<>();
        Set<Long> litIds = new HashSet<>();
        if (userId != null && !shopIds.isEmpty()) {
            favoriteMapper.selectList(new LambdaQueryWrapper<UserFavorite>()
                            .eq(UserFavorite::getUserId, userId)
                            .eq(UserFavorite::getTargetType, "SHOP")
                            .in(UserFavorite::getTargetId, shopIds))
                    .forEach(f -> favoritedIds.add(f.getTargetId()));
            checkinMapper.selectList(new LambdaQueryWrapper<ShopCheckin>()
                            .eq(ShopCheckin::getUserId, userId)
                            .in(ShopCheckin::getShopId, shopIds))
                    .forEach(c -> litIds.add(c.getShopId()));
        }
        for (Shop shop : shops) {
            shop.setCategoryName(categoryNames.get(shop.getCategoryId()));
            shop.setFavorited(favoritedIds.contains(shop.getId()));
            shop.setLit(litIds.contains(shop.getId()));
        }
    }
}
