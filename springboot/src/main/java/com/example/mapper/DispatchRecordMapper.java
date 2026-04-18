package com.example.mapper;

import com.example.entity.DispatchRecord;
import org.apache.ibatis.annotations.Param;

public interface DispatchRecordMapper {
    void insert(DispatchRecord dispatchRecord);

    void updateAcceptStatus(@Param("orderId")Integer orderId, @Param("collectorId")Integer collectorId,@Param("isAccepted")Integer isAccepted);
}
