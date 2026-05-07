-- 校园图书置换系统数据库
CREATE DATABASE IF NOT EXISTS book_exchange DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE book_exchange;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `openid` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `student_id` varchar(20) DEFAULT NULL COMMENT '学号',
  `real_name` varchar(20) DEFAULT NULL COMMENT '真实姓名',
  `college` varchar(50) DEFAULT NULL COMMENT '学院',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系方式',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `credit_score` int(11) DEFAULT 100 COMMENT '信用分',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-未认证 1-已认证 2-封禁',
  `role` tinyint(4) DEFAULT 0 COMMENT '角色: 0-普通用户 1-管理员',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 图书分类表
DROP TABLE IF EXISTS `book_category`;
CREATE TABLE `book_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int(11) DEFAULT 0 COMMENT '排序',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
DROP TABLE IF EXISTS `book`;
CREATE TABLE `book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '发布用户ID',
  `title` varchar(100) NOT NULL COMMENT '书名',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `isbn` varchar(20) DEFAULT NULL COMMENT 'ISBN',
  `publisher` varchar(50) DEFAULT NULL COMMENT '出版社',
  `grade` varchar(20) DEFAULT NULL COMMENT '适用年级',
  `major` varchar(50) DEFAULT NULL COMMENT '适用专业',
  `course_name` varchar(50) DEFAULT NULL COMMENT '课程名称',
  `category_id` bigint(20) DEFAULT NULL COMMENT '分类ID',
  `condition_desc` varchar(255) DEFAULT NULL COMMENT '品相描述',
  `condition_level` tinyint(4) DEFAULT 5 COMMENT '新旧程度1-10',
  `cover_img` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `detail_imgs` varchar(1000) DEFAULT NULL COMMENT '内页照片,逗号分隔',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-待审核 1-上架 2-下架 3-已置换',
  `want_book_desc` varchar(255) DEFAULT NULL COMMENT '想换书籍描述',
  `accept_category` varchar(255) DEFAULT NULL COMMENT '可接受类别,逗号分隔ID',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 置换订单表
DROP TABLE IF EXISTS `exchange_order`;
CREATE TABLE `exchange_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `requester_id` bigint(20) NOT NULL COMMENT '申请人ID',
  `requester_book_id` bigint(20) NOT NULL COMMENT '申请人用来换的书ID',
  `owner_id` bigint(20) NOT NULL COMMENT '图书持有人ID',
  `owner_book_id` bigint(20) NOT NULL COMMENT '被申请置换的书ID',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-待确认 1-已接受 2-已拒绝 3-待交换 4-已完成 5-已取消',
  `exchange_method` varchar(50) DEFAULT NULL COMMENT '交换方式',
  `exchange_location` varchar(100) DEFAULT NULL COMMENT '交换地点',
  `exchange_time` datetime DEFAULT NULL COMMENT '交换时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_requester_id` (`requester_id`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='置换订单表';

-- 消息表
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `content` text COMMENT '消息内容',
  `type` tinyint(4) DEFAULT 0 COMMENT '类型: 0-文字 1-系统通知',
  `is_read` tinyint(4) DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 评价表
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `from_user_id` bigint(20) NOT NULL COMMENT '评价人ID',
  `to_user_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `credit_score` tinyint(4) DEFAULT 5 COMMENT '信用评分1-5',
  `condition_score` tinyint(4) DEFAULT 5 COMMENT '品相评分1-5',
  `attitude_score` tinyint(4) DEFAULT 5 COMMENT '态度评分1-5',
  `content` varchar(500) DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';

-- 收藏表
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `book_id` bigint(20) NOT NULL COMMENT '图书ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_book` (`user_id`, `book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 举报表
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人ID',
  `target_type` tinyint(4) NOT NULL COMMENT '目标类型: 0-用户 1-图书 2-订单',
  `target_id` bigint(20) NOT NULL COMMENT '目标ID',
  `reason` varchar(500) NOT NULL COMMENT '举报原因',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态: 0-待处理 1-已处理 2-已驳回',
  `handle_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- 公告表
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` text NOT NULL COMMENT '内容',
  `admin_id` bigint(20) DEFAULT NULL COMMENT '发布管理员ID',
  `status` tinyint(4) DEFAULT 1 COMMENT '状态: 0-下架 1-上架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 系统通知表
DROP TABLE IF EXISTS `system_notification`;
CREATE TABLE `system_notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `type` tinyint(4) DEFAULT 0 COMMENT '类型: 0-系统 1-置换 2-审核',
  `is_read` tinyint(4) DEFAULT 0 COMMENT '是否已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';
