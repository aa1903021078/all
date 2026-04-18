package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import com.example.entity.CommunityNotice;
import com.example.exception.CustomException;
import com.example.mapper.CommunityNoticeMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommunityNoticeService {

    @Resource
    private CommunityNoticeMapper communityNoticeMapper;

    /**
     * 新增
     */
    public void add(CommunityNotice communityNotice) {
        if (ObjectUtil.isEmpty(communityNotice.getTitle())) {
            throw new CustomException("标题不能为空");
        }
        if (ObjectUtil.isEmpty(communityNotice.getContent())) {
            throw new CustomException("内容不能为空");
        }
        communityNotice.setViewCount(0);
        communityNotice.setIsTop(0);
        communityNotice.setStatus(1);
        communityNotice.setPublishTime(LocalDateTime.now());
        communityNoticeMapper.insert(communityNotice);
    }

    /**
     * 删除
     */
    public void deleteById(Integer id) {
        communityNoticeMapper.deleteById(id);
    }

    /**
     * 修改
     */
    public void updateById(CommunityNotice communityNotice) {
        communityNoticeMapper.updateById(communityNotice);
    }

    /**
     * 根据ID查询
     */
    public CommunityNotice selectById(Integer id) {
        return communityNoticeMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<CommunityNotice> selectAll(CommunityNotice communityNotice) {
        return communityNoticeMapper.selectAll(communityNotice);
    }

    /**
     * 分页查询
     */
    public PageInfo<CommunityNotice> selectPage(CommunityNotice communityNotice, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<CommunityNotice> list = communityNoticeMapper.selectAll(communityNotice);
        return PageInfo.of(list);
    }

    /**
     * 查询置顶公告
     */
    public List<CommunityNotice> selectTop() {
        return communityNoticeMapper.selectTop();
    }

    /**
     * 查询已发布的公告（前端展示用）
     */
    public List<CommunityNotice> selectPublished(Integer type) {
        if (type != null){
            return communityNoticeMapper.selectPublishedByType(type);
        }else {
            return communityNoticeMapper.selectPublishedAll();
        }
    }

    /**
     * 增加浏览次数
     */
    public void increaseView(Integer id) {
        communityNoticeMapper.increaseView(id);
    }

    /**
     * 设置置顶
     */
    public void setTop(Integer id, Integer isTop) {
        CommunityNotice communityNotice = communityNoticeMapper.selectById(id);
        if (ObjectUtil.isNull(communityNotice)) {
            throw new CustomException("公告不存在");
        }
        communityNotice.setIsTop(isTop);
        communityNoticeMapper.updateById(communityNotice);
    }
}
