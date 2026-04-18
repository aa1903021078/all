CREATE DATABASE IF NOT EXISTS `aa09` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `aa09`;
SET NAMES utf8mb4;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `real_name` VARCHAR(50),
  `phone` VARCHAR(20),
  `role` INT NOT NULL COMMENT '0-管理员 1-月嫂 2-营养师 3-护理人员 4-客户',
  `avatar` VARCHAR(255),
  `status` INT DEFAULT 1 COMMENT '0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 服务项目表
-- ----------------------------
DROP TABLE IF EXISTS `service_item`;
CREATE TABLE `service_item` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `image` VARCHAR(255),
  `price` DECIMAL(10,2),
  `status` INT DEFAULT 1 COMMENT '0-下架 1-上架',
  `category` VARCHAR(50) COMMENT '服务分类',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 套餐表
-- ----------------------------
DROP TABLE IF EXISTS `package`;
CREATE TABLE `package` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2),
  `duration` INT COMMENT '服务天数',
  `services` TEXT COMMENT '包含的服务项目描述',
  `status` INT DEFAULT 1 COMMENT '0-下架 1-上架',
  `image` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 订单表
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_no` VARCHAR(50) UNIQUE NOT NULL,
  `customer_id` INT NOT NULL,
  `package_id` INT,
  `total_price` DECIMAL(10,2),
  `status` INT DEFAULT 0 COMMENT '0-待审核 1-已确认 2-服务中 3-已完成 4-已取消',
  `expected_date` DATE,
  `start_date` DATE,
  `end_date` DATE,
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 订单人员分配表
-- ----------------------------
DROP TABLE IF EXISTS `order_staff`;
CREATE TABLE `order_staff` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `staff_role` INT COMMENT '1-月嫂 2-营养师 3-护理人员',
  `status` INT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 生活护理记录表（月嫂填写）
-- ----------------------------
DROP TABLE IF EXISTS `care_record_life`;
CREATE TABLE `care_record_life` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `record_date` DATE,
  `maternal_content` TEXT COMMENT '产妇护理内容',
  `infant_content` TEXT COMMENT '婴儿护理内容',
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 医疗护理记录表（护理人员填写）
-- ----------------------------
DROP TABLE IF EXISTS `care_record_medical`;
CREATE TABLE `care_record_medical` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `record_date` DATE,
  `content` TEXT COMMENT '医疗护理内容',
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 饮食方案表（营养师填写）
-- ----------------------------
DROP TABLE IF EXISTS `diet_plan`;
CREATE TABLE `diet_plan` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `plan_date` DATE,
  `breakfast` TEXT COMMENT '早餐',
  `lunch` TEXT COMMENT '午餐',
  `dinner` TEXT COMMENT '晚餐',
  `snack` TEXT COMMENT '加餐/点心',
  `remark` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 评价表
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `score` INT COMMENT '评分1-5',
  `content` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 投诉表
-- ----------------------------
DROP TABLE IF EXISTS `complaint`;
CREATE TABLE `complaint` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `content` TEXT,
  `reply` TEXT,
  `status` INT DEFAULT 0 COMMENT '0-待处理 1-已处理',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `reply_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 工作人员更换申请表
-- ----------------------------
DROP TABLE IF EXISTS `staff_change_request`;
CREATE TABLE `staff_change_request` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `customer_id` INT NOT NULL,
  `old_staff_id` INT NOT NULL,
  `new_staff_id` INT,
  `reason` TEXT,
  `status` INT DEFAULT 0 COMMENT '0-待审核 1-已批准 2-已拒绝',
  `reply` TEXT,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ----------------------------
-- 初始用户数据
-- ----------------------------
INSERT INTO `user` (`username`, `password`, `real_name`, `phone`, `role`, `status`) VALUES
('admin', '123456', '系统管理员', NULL, 0, 1),
('yuesao1', '123456', '李月嫂', NULL, 1, 1),
('yuesao2', '123456', '王月嫂', NULL, 1, 1),
('yingyang1', '123456', '张营养师', NULL, 2, 1),
('huli1', '123456', '赵护理', NULL, 3, 1),
('kehu1', '123456', '陈女士', '13800138001', 4, 1),
('kehu2', '123456', '刘女士', '13800138002', 4, 1);

-- ----------------------------
-- 服务项目数据
-- ----------------------------
INSERT INTO `service_item` (`name`, `description`, `image`, `price`, `status`, `category`) VALUES
('产后恢复', '专业产后身体恢复指导，包括子宫恢复、骨盆修复、体形恢复等项目，帮助产妇尽快恢复身体机能。', NULL, 2980.00, 1, '产后修复'),
('母乳喂养指导', '资深月嫂一对一母乳喂养指导，解决开奶、堵奶、奶量不足等问题，确保母婴健康。', NULL, 1580.00, 1, '母婴护理'),
('新生儿护理', '专业新生儿日常护理服务，包括洗澡、抚触、脐带护理、黄疸观察等，全方位呵护宝宝健康成长。', NULL, 1980.00, 1, '母婴护理'),
('产后瑜伽', '针对产后妈妈设计的专业瑜伽课程，帮助恢复体形、增强体质、缓解产后疲劳与情绪压力。', NULL, 1280.00, 1, '产后修复'),
('中医调理', '中医专家辨证施治，通过中药膳食、艾灸、推拿等传统手段，调理产后气血、改善体质。', NULL, 3580.00, 1, '健康调理'),
('心理辅导', '专业心理咨询师提供产后心理辅导，预防和缓解产后抑郁，帮助新妈妈建立积极健康的心态。', NULL, 980.00, 1, '健康调理');

-- ----------------------------
-- 套餐数据
-- ----------------------------
INSERT INTO `package` (`name`, `description`, `price`, `duration`, `services`, `status`, `image`) VALUES
('基础套餐', '适合预算有限的家庭，提供基本的月子护理服务，包含月嫂日常照护、基础营养餐及新生儿基本护理。', 19800.00, 26, '月嫂日常照护、基础营养膳食、新生儿基本护理、母乳喂养指导', 1, NULL),
('标准套餐', '全面的月子护理方案，在基础套餐之上增加产后恢复训练、中医调理及营养师定制膳食方案，满足大多数家庭需求。', 39800.00, 28, '月嫂24小时照护、营养师定制膳食、新生儿专业护理、产后恢复训练、母乳喂养指导、中医调理', 1, NULL),
('豪华套餐', '尊享级月子护理体验，涵盖所有服务项目，配备资深月嫂、高级营养师及专属护理团队，另含产后瑜伽与心理辅导。', 69800.00, 42, '资深月嫂24小时专属照护、高级营养师定制膳食、新生儿专业护理、产后恢复全套训练、母乳喂养指导、中医调理、产后瑜伽课程、心理辅导、摄影留念', 1, NULL);
