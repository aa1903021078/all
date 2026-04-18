package com.example.mapper;

import com.example.entity.DailyRecycleStats;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DailyRecycleStatsMapper {


    void insert(DailyRecycleStats stats);

    void updateById(DailyRecycleStats stats);

    DailyRecycleStats selectByDate(String today);

    List<DailyRecycleStats> selectByDateRange(@Param("startDate") String startDate, @Param("endDate")String endDate);
}
