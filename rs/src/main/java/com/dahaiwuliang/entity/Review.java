package com.dahaiwuliang.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long fromUserId;
    private Long toUserId;
    private Integer creditScore;
    private Integer conditionScore;
    private Integer attitudeScore;
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
}
