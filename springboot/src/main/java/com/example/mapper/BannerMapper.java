package com.example.mapper;

import com.example.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerMapper {

    /**
     * 新增
     */
    int insert(Banner banner);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Banner banner);

    /**
     * 根据ID查询
     */
    Banner selectById(Integer id);

    /**
     * 查询所有
     */
    List<Banner> selectAll(Banner banner);

    /**
     * 查询已上架的轮播图（按排序号升序）
     */
    List<Banner> selectPublished();

    /**
     * 根据状态查询
     */
    List<Banner> selectByStatus(@Param("status") Integer status);
}
