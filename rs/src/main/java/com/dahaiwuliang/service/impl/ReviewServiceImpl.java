package com.dahaiwuliang.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dahaiwuliang.entity.Review;
import com.dahaiwuliang.mapper.ReviewMapper;
import com.dahaiwuliang.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
}
