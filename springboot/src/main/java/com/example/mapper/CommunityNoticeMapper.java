package com.example.mapper;

import com.example.entity.CommunityNotice;

import java.util.List;

public interface CommunityNoticeMapper {
    
    void insert(CommunityNotice communityNotice);

    void deleteById(Integer id);

    void updateById(CommunityNotice communityNotice);

    CommunityNotice selectById(Integer id);

    List<CommunityNotice> selectAll(CommunityNotice communityNotice);

    List<CommunityNotice> selectTop();

    List<CommunityNotice> selectPublishedByType(Integer type);

    List<CommunityNotice> selectPublishedAll();

    void increaseView(Integer id);

}
