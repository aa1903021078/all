package com.dahaiwuliang.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dahaiwuliang.entity.Complaint;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ComplaintDao extends BaseMapper<Complaint> {
}
