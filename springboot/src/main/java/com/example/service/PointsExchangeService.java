package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.PointsExchange;
import com.example.entity.PointsGoods;
import com.example.entity.PointsRecord;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.PointsExchangeMapper;
import com.example.mapper.PointsGoodsMapper;
import com.example.mapper.PointsRecordMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 积分兑换业务处理
 */
@Service
public class PointsExchangeService {

    @Resource
    private PointsExchangeMapper pointsExchangeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PointsGoodsMapper pointsGoodsMapper;

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    /**
     * 兑换商品
     */
    @Transactional
    public void exchange(Integer userId, Integer goodsId) {
        User user = userMapper.selectById(userId);
        if (ObjectUtil.isNull(user)) {
            throw new CustomException("用户不存在");
        }

        PointsGoods pointsGoods = pointsGoodsMapper.selectById(goodsId);
        if (ObjectUtil.isNull(pointsGoods)) {
            throw new CustomException("商品不存在");
        }

        if (pointsGoods.getStock() <= 0) {
            throw new CustomException("商品库存不足");
        }

        if (user.getPoints() < pointsGoods.getPointsRequired()) {
            throw new CustomException("积分不足");
        }

        // 扣减积分
        user.setPoints(user.getPoints() - pointsGoods.getPointsRequired());
        userMapper.updateById(user);

        // 减少库存
        pointsGoods.setStock(pointsGoods.getStock() - 1);
        pointsGoods.setTotalExchanged(pointsGoods.getTotalExchanged() + 1);
        pointsGoodsMapper.updateById(pointsGoods);

        // 创建兑换记录
        PointsExchange exchange = new PointsExchange();
        exchange.setUserId(userId);
        exchange.setGoodsId(goodsId);
        exchange.setPointsSpent(pointsGoods.getPointsRequired());
        exchange.setStatus(0); // 待发放
        pointsExchangeMapper.insert(exchange);

        // 添加积分记录
        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(-pointsGoods.getPointsRequired());
        record.setType(2); // 兑换消耗
        record.setDescription("兑换商品：" + pointsGoods.getName());
        record.setBalance(user.getPoints());
        pointsRecordMapper.insert(record);
    }

    /**
     * 查询用户的兑换记录
     */
    public List<PointsExchange> selectByUser(Integer userId) {
        return pointsExchangeMapper.selectByUser(userId);
    }

    /**
     * 分页查询（管理员用）
     */
    public PageInfo<PointsExchange> selectPage(Integer status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<PointsExchange> list = pointsExchangeMapper.selectAll(status);
        return PageInfo.of(list);
    }

    /**
     * 发货（管理员）
     */
    public void updateStatus(Integer id, Integer status) {
        PointsExchange exchange = pointsExchangeMapper.selectById(id);
        if (ObjectUtil.isNull(exchange)) {
            throw new CustomException("记录不存在");
        }
        exchange.setStatus(status);
        pointsExchangeMapper.updateById(exchange);
    }

    /**
     * 取消兑换（退回积分）
     */
    @Transactional
    public void cancel(Integer id) {
        PointsExchange exchange = pointsExchangeMapper.selectById(id);
        if (ObjectUtil.isNull(exchange)) {
            throw new CustomException("记录不存在");
        }
        if (exchange.getStatus() != 0) {
            throw new CustomException("只能取消待发放的订单");
        }

        // 退回积分
        User user = userMapper.selectById(exchange.getUserId());
        user.setPoints(user.getPoints() + exchange.getPointsSpent());
        userMapper.updateById(user);

        // 恢复库存
        PointsGoods pointsGoods = pointsGoodsMapper.selectById(exchange.getGoodsId());
        pointsGoods.setStock(pointsGoods.getStock() + 1);
        pointsGoods.setTotalExchanged(pointsGoods.getTotalExchanged() - 1);
        pointsGoodsMapper.updateById(pointsGoods);

        // 更新状态
        exchange.setStatus(2); // 已取消
        pointsExchangeMapper.updateById(exchange);

        // 添加积分记录
        PointsRecord record = new PointsRecord();
        record.setUserId(user.getId());
        record.setPoints(exchange.getPointsSpent());
        record.setType(4); // 活动奖励（退回）
        record.setDescription("取消兑换退回积分");
        record.setBalance(user.getPoints());
        pointsRecordMapper.insert(record);
    }

}
