package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.dto.CollectorRegisterDTO;
import com.example.entity.Account;
import com.example.entity.Collector;
import com.example.exception.CustomException;
import com.example.mapper.CollectorMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollectorService {

    @Resource
    private CollectorMapper collectorMapper;


    /**
     * 管理员新增回收员
     */
    public void add(Collector collector) {
        String username = collector.getUsername();
        //校验账户是否已经存在
        Collector dbCollector = collectorMapper.selectByUsername(username);
        if (dbCollector != null) {
            throw new CustomException("用户已经存在，请重新注册！");
        }
        // 校验手机号是否已存在
        String phone = collector.getPhone();
        Collector dbCollectorByPhone = collectorMapper.selectByPhone(phone);
        if (dbCollectorByPhone != null) {
            throw new CustomException("手机号已被注册");
        }
        //设置默认值
        if(StrUtil.isBlank(collector.getPassword())){
            //默认密码
            collector.setPassword("123");
        }
        // 默认休息状态
        collector.setWorkStatus(0);
        collector.setRating(new BigDecimal("5.0"));
        collector.setOrderCount(0);
        collector.setTodayOrderCount(0);
        collector.setMaxDailyOrders(10);
        collector.setStatus(1);
        collector.setAuditStatus(1);  // 管理员添加，直接通过

        collectorMapper.insert(collector);
    }

    /**
     * 回收员自主注册（需要审核）
     */
    public void register(CollectorRegisterDTO dto) {
        // 校验两次密码
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致");
        }

        // 校验用户名
        Collector existUser = collectorMapper.selectByUsername(dto.getUsername());
        if (ObjectUtil.isNotNull(existUser)) {
            throw new CustomException("用户名已被注册");
        }

        // 校验手机号
        Collector existPhone = collectorMapper.selectByPhone(dto.getPhone());
        if (ObjectUtil.isNotNull(existPhone)) {
            throw new CustomException("手机号已被注册");
        }

        // 新增：校验身份证号
        Collector existIdCard = collectorMapper.selectByIdCard(dto.getIdCard());
        if (ObjectUtil.isNotNull(existIdCard)) {
            throw new CustomException("身份证号已被注册");
        }

        // 手动设置字段（替代 BeanUtils.copyProperties）
        Collector collector = new Collector();
        collector.setUsername(dto.getUsername());
        collector.setPassword(dto.getPassword());
        collector.setName(dto.getName());
        collector.setPhone(dto.getPhone());
        collector.setIdCard(dto.getIdCard());           // 身份证号
        collector.setIdCardFront(dto.getIdCardFront());   // 身份证正面照片URL
        collector.setIdCardBack(dto.getIdCardBack());     // 身份证反面照片URL
        collector.setServiceArea(dto.getServiceArea());

        // 设置默认值（待审核状态）
        collector.setWorkStatus(0);
        collector.setRating(new BigDecimal("5.0"));
        collector.setOrderCount(0);
        collector.setTodayOrderCount(0);
        collector.setMaxDailyOrders(10);
        collector.setStatus(0);        // 未启用
        collector.setAuditStatus(0);   // 待审核

        collectorMapper.insert(collector);
    }

    /**
     * 查询审核状态
     */
    public Collector queryStatus(String phone, String idCard) {
        List<Collector> list = collectorMapper.selectByPhoneAndIdCard(phone, idCard);
        if (list.isEmpty()) {
            throw new CustomException("未找到申请记录");
        }
        return list.get(0);
    }

    /**
     * 重新提交申请
     */
    public void resubmit(Integer id, CollectorRegisterDTO dto) {
        Collector exist = collectorMapper.selectById(id);
        if (exist == null) {
            throw new CustomException("申请记录不存在");
        }
        if (exist.getAuditStatus() != 2) {
            throw new CustomException("只有被拒绝的申请才能重新提交");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new CustomException("两次输入的密码不一致");
        }
        // 检查新用户名是否被其他用户占用
        Collector userCheck = collectorMapper.selectByUsername(dto.getUsername());
        if (userCheck != null && !userCheck.getId().equals(id)) {
            throw new CustomException("用户名已被其他用户使用");
        }
        // 检查新手机号是否被其他用户占用
        Collector phoneCheck = collectorMapper.selectByPhone(dto.getPhone());
        if (phoneCheck != null && !phoneCheck.getId().equals(id)) {
            throw new CustomException("手机号已被其他用户使用");
        }
        // 更新信息
        exist.setUsername(dto.getUsername());
        exist.setPassword(dto.getPassword());
        exist.setName(dto.getName());
        exist.setIdCard(dto.getIdCard());
        exist.setIdCardFront(dto.getIdCardFront());
        exist.setIdCardBack(dto.getIdCardBack());
        exist.setPhone(dto.getPhone());
        exist.setServiceArea(dto.getServiceArea());
        exist.setAuditStatus(0);
        exist.setRejectReason(null);
        collectorMapper.updateById(exist);
    }

    /**
     * 管理员审核通过
     */
    public void auditPass(Integer id) {
        Collector collector = collectorMapper.selectById(id);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }
        if (collector.getAuditStatus() != 0) {
            throw new CustomException("该申请已处理");
        }
        collectorMapper.auditPass(id);
    }

    /**
     * 管理员审核拒绝
     */
    public void auditReject(Integer id, String reason) {
        Collector collector = collectorMapper.selectById(id);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }
        if (collector.getAuditStatus() != 0) {
            throw new CustomException("该申请已处理");
        }
        if (ObjectUtil.isEmpty(reason)) {
            reason = "不符合要求";
        }
        collectorMapper.auditReject(id, reason);
    }

    /**
     * 查询待审核列表
     */
    public List<Collector> selectPendingAudit() {
        return collectorMapper.selectPendingAudit();
    }


    /**
     * 删除
     */
    public void deleteById(Integer id) {
        collectorMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Collector collector) {
        collectorMapper.updateById(collector);
    }

    /**
     * 更新工作状态
     */
    public void updateWorkStatus(Integer id, Integer workStatus) {
        Collector collector = collectorMapper.selectById(id);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }
        // 校验状态值
        if (workStatus < 0 || workStatus > 2) {
            throw new CustomException("工作状态参数错误");
        }
        collector.setWorkStatus(workStatus);
        collectorMapper.updateById(collector);
    }

    /**
     * 更新位置信息
     */
    public void updateLocation(Integer id, Double lat, Double lng) {
        Collector collector = collectorMapper.selectById(id);
        if (ObjectUtil.isNull(collector)) {
            throw new CustomException("回收员不存在");
        }
        collector.setLocationLat(new BigDecimal(lat));
        collector.setLocationLng(new BigDecimal(lng));
        collector.setLocationUpdateTime(LocalDateTime.now());
        collectorMapper.updateById(collector);
    }

    /**
     * 根据ID查询
     */
    public Collector selectById(Integer id) {
        return collectorMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Collector> selectAll(Collector collector) {
        return collectorMapper.selectAll(collector);
    }

    /**
     * 分页查询的方法
     *
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    public PageInfo<Collector> selectPage(Collector collector, Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        List<Collector> list = collectorMapper.selectAll(collector);
        return PageInfo.of(list);
    }

    /**
     * 查询附近可用的回收员
     */
    public List<Collector> selectNearby(Double lat, Double lng, Double radius) {
        // 查询工作状态为接单中的回收员
        return collectorMapper.selectNearby(lat, lng, radius);
    }

    /**
     * 回收员登录
     */
    public Collector login(Account account) {
        Collector dbCollector = collectorMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbCollector)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbCollector.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        if (dbCollector.getStatus() != 1) {
            throw new CustomException("账号已被禁用");
        }
        return dbCollector;
    }

    /**
     * 修改密码
     * @param account
     */
    public  void updatePassword(Account account) {
        Collector dbCollector = collectorMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbCollector)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbCollector.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbCollector.setPassword(account.getNewPassword());
        collectorMapper.updateById(dbCollector);
    }

    /**
     * 获取今日统计
     */
    public Map<String, Object> getTodayStats(Integer id) {
        Collector collector = collectorMapper.selectById(id);
        Map<String, Object> stats = new HashMap<>();
        stats.put("todayOrderCount", collector.getTodayOrderCount());
        stats.put("totalOrderCount", collector.getOrderCount());
        stats.put("rating", collector.getRating());
        stats.put("maxDailyOrders", collector.getMaxDailyOrders());
        return stats;
    }

    /**
     * 增加接单数量
     */
    public void incrementOrderCount(Integer id) {
        Collector collector = collectorMapper.selectById(id);
        if (ObjectUtil.isNotNull(collector)) {
            collector.setOrderCount(collector.getOrderCount() + 1);
            collector.setTodayOrderCount(collector.getTodayOrderCount() + 1);
            collectorMapper.updateById(collector);
        }
    }

    /**
     * 重置今日接单数（定时任务调用）
     */
    public void resetTodayOrderCount() {
        collectorMapper.resetTodayOrderCount();
    }
}
