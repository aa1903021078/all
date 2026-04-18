package com.example.mapper;

import com.example.entity.ApplianceType;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 操作appliance_type相关数据接口
 */
public interface ApplianceTypeMapper {

    /**
     * 新增
     */
    int insert(ApplianceType applianceType);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(ApplianceType applianceType);

    /**
     * 根据ID查询
     */
    ApplianceType selectById(Integer id);

    /**
     * 查询所有
     */
    List<ApplianceType> selectAll(ApplianceType applianceType);

    /**
     * 查询启用的品类列表（前端展示用）
     */
    List<ApplianceType> selectEnabled();

    /**
     * 根据名称查询
     */
    ApplianceType selectByName(String name);
}
