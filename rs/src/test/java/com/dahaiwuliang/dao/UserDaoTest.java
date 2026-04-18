//package com.dahaiwuliang.dao;
//
//import com.dahaiwuliang.vo.UserTestVO;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.Date;
//import javax.annotation.Resource;
//
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class UserDaoTest {
//
//    @Resource
//    private UserMapper userMapper;
//
//    @Test
//    public void testInsert(){
//        String dateString = "2024-07-13 15:30:00";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime dateTime = LocalDateTime.parse(dateString, formatter);
//
//        UserTestVO userTestVO = new UserTestVO();
//        userTestVO.setUuid("1");
//        userTestVO.setMin("0.8");
//        userTestVO.setMax("34");
//        userTestVO.setStartdate(dateTime);
//        userTestVO.setEnddate(dateTime);
//
//        userMapper.insert(userTestVO);
//
//    }
//
//
//}
//
//
//
//
//
//
//
//
//
//
//
