package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.PointsGoods;
import com.example.exception.CustomException;
import com.example.mapper.PointsGoodsMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分商品业务处理
 */
@Service
public class PointsGoodsService {

    @Resource
    private PointsGoodsMapper pointsGoodsMapper;

    /**
     *新增兑换商品
     */
    public void add(PointsGoods pointsGoods) {
        if (ObjectUtil.isEmpty(pointsGoods.getName())) {
            throw new CustomException("商品名称不能为空");
        }
        if (pointsGoods.getPointsRequired() == null || pointsGoods.getPointsRequired() <= 0) {
            throw new CustomException("所需积分必须大于0");
        }
        pointsGoods.setStock(ObjectUtil.isNull(pointsGoods.getStock()) ? 0 : pointsGoods.getStock());
        pointsGoods.setTotalExchanged(0);
        pointsGoods.setStatus(1);
        pointsGoodsMapper.insert(pointsGoods);
    }

    /**
     *删除兑换商品
     */
    public void deleteById(Integer id) {
        pointsGoodsMapper.deleteById(id);
    }

    /**
     *更新兑换商品
     */
    public void updateById(PointsGoods pointsGoods) {
        pointsGoodsMapper.updateById(pointsGoods);
    }

    /**
     * 根据ID查询
     */
    public PointsGoods selectById(Integer id) {
        return pointsGoodsMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<PointsGoods> selectAll(PointsGoods pointsGoods) {
        return pointsGoodsMapper.selectAll(pointsGoods);
    }

    /**
     *分页查询
     */
    public PageInfo<PointsGoods> selectPage(PointsGoods pointsGoods, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PointsGoods> list = pointsGoodsMapper.selectAll(pointsGoods);
        return PageInfo.of(list);
    }

    /**
     * 查询可兑换的商品（库存>0且上架）
     */
    public List<PointsGoods> selectAvailable() {
        return pointsGoodsMapper.selectAvailable();
    }

    /**
     * 兑换商品时减少库存
     */
    public void decreaseStock(Integer goodsId) {
        // 1. 查询商品信息（带版本号）
        PointsGoods pointsGoods = pointsGoodsMapper.selectByIdWithVersion(goodsId);
        if (ObjectUtil.isNull(pointsGoods)) {
            throw new CustomException("商品不存在");
        }
        if (pointsGoods.getStock() <= 0) {
            throw new CustomException("商品库存不足");
        }

        Integer oldVersion = pointsGoods.getVersion();

        // 2. 乐观锁扣减库存（CAS操作）
        int affected = pointsGoodsMapper.decreaseStockWithVersion(goodsId, oldVersion);

        // 3. 判断更新结果（影响行数为0表示版本冲突）
        if (affected == 0) {
            throw new CustomException("系统繁忙，请稍后重试"); // 或进行重试
        }
    }
}
