package com.example.mapper;

import com.example.entity.Collector;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作collector相关数据接口
 */
public interface CollectorMapper {

    /**
     * 新增
     */
    void insert(Collector collector);

    /**
     * 删除
     */
    void deleteById(Integer id);

    /**
     * 修改
     */
    void updateById(Collector collector);

    /**
     * 根据ID查询
     */
    Collector selectById(Integer id);

    /**
     * 查询所有
     */
    List<Collector> selectAll(Collector collector);

    /**
     * 根据用户名查询
     */
    Collector selectByUsername(String username);

    /**
     * 根据手机号查询
     */
    Collector selectByPhone(String phone);

    /**
     * 根据手机号和身份证号查询（新增）
     */
    List<Collector> selectByPhoneAndIdCard(@Param("phone") String phone, @Param("idCard") String idCard);

    /**
     * 根据身份证号查询
     */
    Collector selectByIdCard(String idCard);

    /**
     * 查询附近可用的回收员
     */
    List<Collector> selectNearby(@Param("lat")Double lat,@Param("lng")Double lng,@Param("radius")Double radius);

    /**
     * 查询待审核的回收员
     */
    List<Collector> selectPendingAudit();

    /**
     * 审核通过
     */
    void auditPass(Integer id);

    /**
     * 审核拒绝
     */
    void auditReject(@Param("id")Integer id, @Param("reason")String reason);

    /**
     * 重置今日接单数
     */
    void resetTodayOrderCount();



}
