package com.dahaiwuliang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dahaiwuliang.entity.Note;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 探店笔记 Mapper
 */
public interface NoteMapper extends BaseMapper<Note> {

    /** 笔记发布趋势 (date=日期, value=数量) */
    @Select("SELECT DATE(create_time) AS date, COUNT(*) AS value FROM note " +
            "WHERE status = 1 GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> publishTrend();
}
