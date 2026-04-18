package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.Banner;
import com.example.exception.CustomException;
import com.example.mapper.BannerMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 轮播图业务处理
 */
@Service
public class BannerService {

    @Resource
    private BannerMapper bannerMapper;

    /**
     * 新增
     */
    public void add(Banner banner) {
        // 校验标题不能为空
        if (ObjectUtil.isEmpty(banner.getTitle())) {
            throw new CustomException("标题不能为空");
        }
        // 设置默认值
        if (ObjectUtil.isEmpty(banner.getSortOrder())) {
            banner.setSortOrder(0);
        }
        if (ObjectUtil.isEmpty(banner.getStatus())) {
            banner.setStatus(1); // 默认上架
        }
        if (ObjectUtil.isEmpty(banner.getIcon())) {
            banner.setIcon("🌍");
        }
        bannerMapper.insert(banner);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        bannerMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(Banner banner) {
        if (banner.getId() == null) {
            throw new CustomException("ID不能为空");
        }
        bannerMapper.updateById(banner);
    }

    /**
     * 根据ID查询
     */
    public Banner selectById(Integer id) {
        return bannerMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Banner> selectAll(Banner banner) {
        return bannerMapper.selectAll(banner);
    }

    /**
     * 分页查询
     */
    public PageInfo<Banner> selectPage(Banner banner, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Banner> list = bannerMapper.selectAll(banner);
        return PageInfo.of(list);
    }

    /**
     * 获取已上架的轮播图列表（前端展示用）
     */
    public List<Banner> getPublishedBanners() {
        return bannerMapper.selectPublished();
    }

    /**
     * 根据状态查询
     */
    public List<Banner> selectByStatus(Integer status) {
        return bannerMapper.selectByStatus(status);
    }

    /**
     * 上架/下架
     */
    public void toggleStatus(Integer id, Integer status) {
        Banner banner = new Banner();
        banner.setId(id);
        banner.setStatus(status);
        bannerMapper.updateById(banner);
    }
}
