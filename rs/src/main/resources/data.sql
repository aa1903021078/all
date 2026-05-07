USE book_exchange;

-- 初始化管理员账号 (密码: admin123, MD5加密)
INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar`, `student_id`, `real_name`, `college`, `phone`, `password`, `credit_score`, `status`, `role`) VALUES
(1, 'admin_openid_001', '系统管理员', '/uploads/default_avatar.png', 'ADMIN001', '管理员', '信息技术学院', '13800000000', 'e10adc3949ba59abbe56e057f20f883e', 100, 1, 1);

-- 初始化测试用户
INSERT INTO `user` (`id`, `openid`, `nickname`, `avatar`, `student_id`, `real_name`, `college`, `phone`, `password`, `credit_score`, `status`, `role`) VALUES
(2, 'test_openid_001', '张三', '/uploads/default_avatar.png', '2021001001', '张三', '计算机学院', '13800000001', 'e10adc3949ba59abbe56e057f20f883e', 100, 1, 0),
(3, 'test_openid_002', '李四', '/uploads/default_avatar.png', '2021001002', '李四', '数学学院', '13800000002', 'e10adc3949ba59abbe56e057f20f883e', 95, 1, 0),
(4, 'test_openid_003', '王五', '/uploads/default_avatar.png', '2021001003', '王五', '外语学院', '13800000003', 'e10adc3949ba59abbe56e057f20f883e', 88, 1, 0);

-- 初始化图书分类
INSERT INTO `book_category` (`id`, `name`, `sort`) VALUES
(1, '教材类', 1),
(2, '教辅类', 2),
(3, '课外书', 3),
(4, '考试类', 4),
(5, '工具书', 5);

-- 初始化图书数据
INSERT INTO `book` (`id`, `user_id`, `title`, `author`, `isbn`, `publisher`, `grade`, `major`, `course_name`, `category_id`, `condition_desc`, `condition_level`, `cover_img`, `status`, `want_book_desc`, `accept_category`) VALUES
(1, 2, '数据结构与算法分析', '严蔚敏', '9787302330646', '清华大学出版社', '大二', '计算机科学', '数据结构', 1, '九成新，无笔记无划线', 9, '/uploads/book_cover_1.jpg', 1, '想换一本操作系统相关教材', '1,2'),
(2, 2, '高等数学（上册）', '同济大学', '9787040396638', '高等教育出版社', '大一', '全校通用', '高等数学', 1, '八成新，有少量铅笔笔记', 8, '/uploads/book_cover_2.jpg', 1, '想换线性代数或概率论教材', '1'),
(3, 3, '大学英语精读4', '董亚芬', '9787544631570', '上海外语教育出版社', '大二', '全校通用', '大学英语', 1, '七成新，有笔记和划线', 7, '/uploads/book_cover_3.jpg', 1, '想换任意课外书', '3'),
(4, 3, '计算机网络（第7版）', '谢希仁', '9787121302954', '电子工业出版社', '大三', '计算机科学', '计算机网络', 1, '全新未拆封', 10, '/uploads/book_cover_4.jpg', 1, '想换数据库或软件工程教材', '1,2'),
(5, 4, '考研英语真题解析', '张剑', '9787501256891', '世界知识出版社', '大四', '全校通用', '考研英语', 4, '九成新，做了部分题目', 9, '/uploads/book_cover_5.jpg', 1, '想换考研政治资料', '4'),
(6, 4, '活着', '余华', '9787506365437', '作家出版社', '不限', '不限', '', 3, '全新，朋友送的重复了', 10, '/uploads/book_cover_6.jpg', 1, '想换任意小说或文学类书籍', '3');

-- 初始化置换订单
INSERT INTO `exchange_order` (`id`, `requester_id`, `requester_book_id`, `owner_id`, `owner_book_id`, `status`, `exchange_method`, `exchange_location`) VALUES
(1, 3, 3, 2, 1, 4, '校内自提', '图书馆一楼大厅'),
(2, 4, 5, 3, 4, 0, NULL, NULL);

-- 初始化评价
INSERT INTO `review` (`order_id`, `from_user_id`, `to_user_id`, `credit_score`, `condition_score`, `attitude_score`, `content`) VALUES
(1, 3, 2, 5, 5, 5, '书的品相很好，沟通很愉快，推荐！'),
(1, 2, 3, 4, 4, 5, '交换顺利，书本状态和描述一致');

-- 初始化公告
INSERT INTO `announcement` (`title`, `content`, `admin_id`, `status`) VALUES
('欢迎使用校园图书置换平台', '本平台致力于为在校学生提供便捷的二手图书置换服务，实现资源共享、绿色环保。请大家诚信交易，文明沟通。', 1, 1),
('置换规则说明', '1. 请确保发布的图书信息真实准确；\n2. 置换前请充分沟通确认图书品相；\n3. 建议在校园内公共场所进行交换；\n4. 交换完成后请及时确认并互相评价；\n5. 如遇纠纷请联系管理员协调处理。', 1, 1);

-- 初始化收藏
INSERT INTO `favorite` (`user_id`, `book_id`) VALUES
(2, 4), (2, 5), (3, 1), (4, 2);
