-- ==========================================================================
-- 美食探店 & 菜谱分享平台  数据库初始化脚本 (MySQL 8.x)
-- 数据库: foodie   字符集: utf8mb4
-- 默认账号密码均为: 123456
-- 导入: mysql -uroot -p < foodie.sql
-- ==========================================================================
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `foodie` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `foodie`;

-- ---------------------------- 1. 用户 ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码',
  `nickname`    VARCHAR(64)           DEFAULT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像',
  `phone`       VARCHAR(20)           DEFAULT NULL,
  `email`       VARCHAR(64)           DEFAULT NULL,
  `gender`      TINYINT               DEFAULT 0 COMMENT '0未知 1男 2女',
  `bio`         VARCHAR(255)          DEFAULT NULL COMMENT '个性签名',
  `status`      TINYINT               DEFAULT 0 COMMENT '0正常 1封禁',
  `create_time` DATETIME              DEFAULT NULL,
  `update_time` DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- ---------------------------- 2. 角色 (RBAC) ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(64) NOT NULL COMMENT '角色名',
  `code`        VARCHAR(64) NOT NULL COMMENT '角色编码',
  `remark`      VARCHAR(255)         DEFAULT NULL,
  `create_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色';

DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id`     BIGINT      NOT NULL AUTO_INCREMENT,
  `name`   VARCHAR(64) NOT NULL COMMENT '权限名',
  `code`   VARCHAR(64) NOT NULL COMMENT '权限编码 如 content:review',
  `remark` VARCHAR(255)         DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限';

DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id`            BIGINT NOT NULL AUTO_INCREMENT,
  `role_id`       BIGINT NOT NULL,
  `permission_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限';

-- ---------------------------- 3. 菜系分类 ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(32) NOT NULL COMMENT '菜系名',
  `icon`        VARCHAR(32)          DEFAULT NULL COMMENT '图标(emoji)',
  `sort`        INT                  DEFAULT 0,
  `create_time` DATETIME             DEFAULT NULL,
  `update_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜系分类';

-- ---------------------------- 4. 店铺 ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(128)  NOT NULL COMMENT '店铺名',
  `category_id`    BIGINT                 DEFAULT NULL COMMENT '菜系',
  `address`        VARCHAR(255)           DEFAULT NULL,
  `longitude`      DECIMAL(10,6)          DEFAULT NULL COMMENT '经度',
  `latitude`       DECIMAL(10,6)          DEFAULT NULL COMMENT '纬度',
  `avg_price`      DECIMAL(10,2)          DEFAULT 0 COMMENT '人均消费',
  `cover`          VARCHAR(255)           DEFAULT NULL COMMENT '封面',
  `images`         TEXT                   COMMENT '实拍图集 JSON 数组',
  `business_hours` VARCHAR(64)            DEFAULT NULL COMMENT '营业时间',
  `phone`          VARCHAR(20)            DEFAULT NULL COMMENT '联系电话',
  `description`    TEXT                   COMMENT '简介',
  `merchant_id`    BIGINT                 DEFAULT NULL COMMENT '所属商家',
  `status`         TINYINT                DEFAULT 1 COMMENT '0待审核 1上架 2下架 3拒绝',
  `rating`         DECIMAL(2,1)           DEFAULT 0 COMMENT '综合评分',
  `rating_count`   INT                    DEFAULT 0 COMMENT '评分数',
  `view_count`     INT                    DEFAULT 0 COMMENT '曝光/访客',
  `checkin_count`  INT                    DEFAULT 0 COMMENT '点亮人数',
  `recommend`      TINYINT                DEFAULT 0 COMMENT '首页推荐',
  `create_time`    DATETIME               DEFAULT NULL,
  `update_time`    DATETIME               DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_merchant` (`merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='店铺';

-- ---------------------------- 5. 推荐菜品 ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `shop_id`     BIGINT        NOT NULL,
  `name`        VARCHAR(64)   NOT NULL,
  `price`       DECIMAL(10,2)          DEFAULT 0,
  `image`       VARCHAR(255)           DEFAULT NULL,
  `description` VARCHAR(255)           DEFAULT NULL,
  `sort`        INT                    DEFAULT 0,
  `create_time` DATETIME               DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='推荐菜品';

-- ---------------------------- 6. 菜谱 ----------------------------
DROP TABLE IF EXISTS `recipe`;
CREATE TABLE `recipe` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `title`          VARCHAR(128) NOT NULL COMMENT '菜谱名',
  `cover`          VARCHAR(255)          DEFAULT NULL,
  `author_id`      BIGINT                DEFAULT NULL,
  `category_id`    BIGINT                DEFAULT NULL COMMENT '菜系',
  `cook_time`      INT                   DEFAULT 0 COMMENT '烹饪时长(分钟)',
  `difficulty`     TINYINT               DEFAULT 1 COMMENT '1简单 2中等 3困难',
  `description`    TEXT                  COMMENT '富文本简介',
  `status`         TINYINT               DEFAULT 1 COMMENT '0待审核 1发布 2下架 3违规',
  `view_count`     INT                   DEFAULT 0,
  `like_count`     INT                   DEFAULT 0,
  `favorite_count` INT                   DEFAULT 0,
  `recommend`      TINYINT               DEFAULT 0 COMMENT '首页推荐',
  `create_time`    DATETIME              DEFAULT NULL,
  `update_time`    DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_author` (`author_id`),
  KEY `idx_cate` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱';

DROP TABLE IF EXISTS `recipe_ingredient`;
CREATE TABLE `recipe_ingredient` (
  `id`        BIGINT      NOT NULL AUTO_INCREMENT,
  `recipe_id` BIGINT      NOT NULL,
  `name`      VARCHAR(64) NOT NULL COMMENT '食材名',
  `amount`    VARCHAR(64)          DEFAULT NULL COMMENT '用量',
  `sort`      INT                  DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_recipe` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱食材';

DROP TABLE IF EXISTS `recipe_step`;
CREATE TABLE `recipe_step` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `recipe_id` BIGINT NOT NULL,
  `step_no`   INT             DEFAULT 1 COMMENT '步骤序号',
  `image`     VARCHAR(255)    DEFAULT NULL,
  `content`   TEXT            COMMENT '步骤说明',
  `sort`      INT             DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_recipe_step` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱步骤';

-- ---------------------------- 7. 探店笔记 ----------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `shop_id`       BIGINT                DEFAULT NULL,
  `author_id`     BIGINT                DEFAULT NULL,
  `title`         VARCHAR(128)          DEFAULT NULL,
  `content`       TEXT                  COMMENT '富文本内容',
  `images`        TEXT                  COMMENT '图集 JSON 数组',
  `rating`        TINYINT               DEFAULT 5 COMMENT '作者评分 1-5',
  `status`        TINYINT               DEFAULT 1 COMMENT '0待审核 1发布 2下架 3违规',
  `like_count`    INT                   DEFAULT 0,
  `comment_count` INT                   DEFAULT 0,
  `view_count`    INT                   DEFAULT 0,
  `recommend`     TINYINT               DEFAULT 0 COMMENT '首页置顶推荐',
  `create_time`   DATETIME              DEFAULT NULL,
  `update_time`   DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_shop_note` (`shop_id`),
  KEY `idx_author_note` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='探店笔记';

-- ---------------------------- 8. 评论 (多级) ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `target_type`   VARCHAR(16)   NOT NULL COMMENT 'NOTE/RECIPE',
  `target_id`     BIGINT        NOT NULL,
  `user_id`       BIGINT        NOT NULL,
  `parent_id`     BIGINT                 DEFAULT 0 COMMENT '父评论 0为顶级',
  `reply_user_id` BIGINT                 DEFAULT NULL COMMENT '被回复用户',
  `content`       VARCHAR(1000) NOT NULL,
  `like_count`    INT                    DEFAULT 0,
  `create_time`   DATETIME               DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论';

-- ---------------------------- 9. 点赞 / 收藏 / 点亮 ----------------------------
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `target_type` VARCHAR(16) NOT NULL COMMENT 'NOTE/RECIPE/COMMENT/REPOST',
  `target_id`   BIGINT      NOT NULL,
  `create_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_like` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞';

DROP TABLE IF EXISTS `favorite_folder`;
CREATE TABLE `favorite_folder` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `name`        VARCHAR(64) NOT NULL COMMENT '收藏夹名',
  `type`        VARCHAR(16) NOT NULL COMMENT 'SHOP/RECIPE',
  `create_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_folder` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹';

DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `target_type` VARCHAR(16) NOT NULL COMMENT 'SHOP/RECIPE',
  `target_id`   BIGINT      NOT NULL,
  `folder_id`   BIGINT               DEFAULT 0,
  `create_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fav` (`user_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';

DROP TABLE IF EXISTS `shop_checkin`;
CREATE TABLE `shop_checkin` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT   NOT NULL,
  `shop_id`      BIGINT   NOT NULL,
  `note_id`      BIGINT            DEFAULT 0,
  `checkin_time` DATETIME          DEFAULT NULL,
  `create_time`  DATETIME          DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_checkin` (`user_id`, `shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点亮店铺';

-- ---------------------------- 10. 预约 ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation` (
  `id`            BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT      NOT NULL,
  `shop_id`       BIGINT      NOT NULL,
  `reserve_time`  DATETIME             DEFAULT NULL COMMENT '预约到店时间',
  `people_count`  INT                  DEFAULT 1,
  `contact_name`  VARCHAR(64)          DEFAULT NULL,
  `contact_phone` VARCHAR(20)          DEFAULT NULL,
  `remark`        VARCHAR(255)         DEFAULT NULL,
  `status`        TINYINT              DEFAULT 0 COMMENT '0待确认 1已确认 2已拒绝 3已完成 4已取消',
  `create_time`   DATETIME             DEFAULT NULL,
  `update_time`   DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_res` (`user_id`),
  KEY `idx_shop_res` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约';

-- ---------------------------- 11. 复刻晒图 ----------------------------
DROP TABLE IF EXISTS `recipe_repost`;
CREATE TABLE `recipe_repost` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `recipe_id`   BIGINT        NOT NULL,
  `user_id`     BIGINT        NOT NULL,
  `images`      TEXT          COMMENT '成品图 JSON',
  `content`     VARCHAR(1000)          DEFAULT NULL COMMENT '心得',
  `like_count`  INT                    DEFAULT 0,
  `create_time` DATETIME               DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_recipe_repost` (`recipe_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜谱复刻晒图';

-- ---------------------------- 12. 食材采购清单 ----------------------------
DROP TABLE IF EXISTS `shopping_item`;
CREATE TABLE `shopping_item` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `name`        VARCHAR(64) NOT NULL COMMENT '食材名',
  `amount`      VARCHAR(64)          DEFAULT NULL COMMENT '用量',
  `recipe_id`   BIGINT               DEFAULT 0 COMMENT '来源菜谱',
  `status`      TINYINT              DEFAULT 0 COMMENT '0待采购 1家中已有 2已核销',
  `create_time` DATETIME             DEFAULT NULL,
  `update_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_shopping` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食材采购清单';

-- ---------------------------- 13. 实时聊天 ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT       NOT NULL COMMENT '用户',
  `merchant_id`     BIGINT       NOT NULL COMMENT '商家',
  `shop_id`         BIGINT                DEFAULT 0,
  `last_message`    VARCHAR(255)          DEFAULT NULL,
  `last_time`       DATETIME              DEFAULT NULL,
  `user_unread`     INT                   DEFAULT 0,
  `merchant_unread` INT                   DEFAULT 0,
  `create_time`     DATETIME              DEFAULT NULL,
  `update_time`     DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session` (`user_id`, `merchant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话';

DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `session_id`   BIGINT        NOT NULL,
  `from_user_id` BIGINT        NOT NULL,
  `to_user_id`   BIGINT        NOT NULL,
  `content`      VARCHAR(1000) NOT NULL,
  `type`         VARCHAR(16)            DEFAULT 'text',
  `is_read`      TINYINT                DEFAULT 0,
  `create_time`  DATETIME               DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息';

-- ---------------------------- 14. 系统配置 / 搜索热词 ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `config_key`   VARCHAR(64) NOT NULL,
  `config_value` VARCHAR(500)         DEFAULT NULL,
  `remark`       VARCHAR(255)         DEFAULT NULL,
  `update_time`  DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置';

DROP TABLE IF EXISTS `search_keyword`;
CREATE TABLE `search_keyword` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `keyword`     VARCHAR(64) NOT NULL,
  `count`       INT                  DEFAULT 1 COMMENT '搜索次数',
  `update_time` DATETIME             DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索热词';

SET FOREIGN_KEY_CHECKS = 1;

-- ==========================================================================
-- 初始化测试数据 (所有账号密码: 123456)
-- ==========================================================================
USE `foodie`;

-- 用户 (密码 BCrypt: 123456)
INSERT INTO `sys_user` (`id`,`username`,`password`,`nickname`,`avatar`,`phone`,`email`,`gender`,`bio`,`status`,`create_time`,`update_time`) VALUES
(1,'admin','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','超级管理员','https://picsum.photos/seed/avatar1/100/100','13800000001','admin@foodie.com',1,'平台超级管理员',0,NOW(),NOW()),
(2,'reviewer','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','内容审核员','https://picsum.photos/seed/avatar2/100/100','13800000002','review@foodie.com',2,'负责内容审核',0,NOW(),NOW()),
(3,'merchant1','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','老王川菜','https://picsum.photos/seed/avatar3/100/100','13800000003','wang@foodie.com',1,'正宗川味 麻辣鲜香',0,NOW(),NOW()),
(4,'merchant2','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','阿婆粤茶','https://picsum.photos/seed/avatar4/100/100','13800000004','po@foodie.com',2,'老广早茶 一盅两件',0,NOW(),NOW()),
(5,'user1','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','吃货小李','https://picsum.photos/seed/avatar5/100/100','13800000005','li@foodie.com',1,'用美食记录生活',0,NOW(),NOW()),
(6,'user2','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','美食家Amy','https://picsum.photos/seed/avatar6/100/100','13800000006','amy@foodie.com',2,'探店达人 打卡100家',0,NOW(),NOW()),
(7,'user3','$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq','深夜食堂','https://picsum.photos/seed/avatar7/100/100','13800000007','ben@foodie.com',1,'专注夜宵三十年',0,NOW(),NOW());

-- 角色
INSERT INTO `sys_role` (`id`,`name`,`code`,`remark`,`create_time`) VALUES
(1,'超级管理员','ADMIN','拥有全部权限',NOW()),
(2,'审核员','REVIEWER','仅内容审核相关权限',NOW()),
(3,'商家','MERCHANT','商家端权限',NOW()),
(4,'普通用户','USER','用户端权限',NOW());

-- 用户-角色
INSERT INTO `sys_user_role` (`user_id`,`role_id`) VALUES
(1,1),(2,2),(3,3),(3,4),(4,3),(4,4),(5,4),(6,4),(7,4);

-- 权限
INSERT INTO `sys_permission` (`id`,`name`,`code`,`remark`) VALUES
(1,'全部权限','*:*:*','超级管理员'),
(2,'内容审核','content:review','笔记/菜谱审核'),
(3,'内容管理','content:manage','内容下架删除'),
(4,'店铺管理','shop:manage','平台店铺管理'),
(5,'用户管理','user:manage','用户封禁'),
(6,'运营管理','operation:manage','推荐位配置'),
(7,'系统配置','config:manage','系统参数/RBAC'),
(8,'数据大屏','dashboard:view','平台数据看板');

-- 角色-权限 (ADMIN=全部, REVIEWER=审核+内容+大屏)
INSERT INTO `sys_role_permission` (`role_id`,`permission_id`) VALUES
(1,1),
(2,2),(2,3),(2,8);

-- 菜系分类
INSERT INTO `category` (`id`,`name`,`icon`,`sort`,`create_time`,`update_time`) VALUES
(1,'川菜','🌶️',1,NOW(),NOW()),
(2,'粤菜','🦐',2,NOW(),NOW()),
(3,'火锅','🍲',3,NOW(),NOW()),
(4,'日料','🍣',4,NOW(),NOW()),
(5,'西餐','🍝',5,NOW(),NOW()),
(6,'烧烤','🍢',6,NOW(),NOW()),
(7,'甜品','🍰',7,NOW(),NOW()),
(8,'小吃','🍜',8,NOW(),NOW()),
(9,'湘菜','🌶',9,NOW(),NOW()),
(10,'咖啡','☕',10,NOW(),NOW());

-- 店铺 (北京坐标, status:1上架 0待审核)
INSERT INTO `shop` (`id`,`name`,`category_id`,`address`,`longitude`,`latitude`,`avg_price`,`cover`,`images`,`business_hours`,`phone`,`description`,`merchant_id`,`status`,`rating`,`rating_count`,`view_count`,`checkin_count`,`recommend`,`create_time`,`update_time`) VALUES
(1,'老王川菜馆',1,'北京市东城区王府井大街18号',116.407526,39.910925,88.00,'https://picsum.photos/seed/shop1/640/480','["https://picsum.photos/seed/shop1a/640/480","https://picsum.photos/seed/shop1b/640/480"]','10:00-22:00','010-88880001','正宗川味,麻辣鲜香,水煮鱼一绝',3,1,4.8,120,3200,86,1,NOW(),NOW()),
(2,'阿婆粤式茶楼',2,'北京市朝阳区三里屯路11号',116.447847,39.937193,128.00,'https://picsum.photos/seed/shop2/640/480','["https://picsum.photos/seed/shop2a/640/480","https://picsum.photos/seed/shop2b/640/480"]','07:00-15:00','010-88880002','老广早茶,虾饺烧卖,一盅两件',4,1,4.6,98,2800,72,1,NOW(),NOW()),
(3,'蜀韵火锅(国贸店)',3,'北京市朝阳区国贸CBD商圈',116.457314,39.921984,158.00,'https://picsum.photos/seed/shop3/640/480','["https://picsum.photos/seed/shop3a/640/480"]','11:00-02:00','010-88880003','麻辣牛油锅底,毛肚鲜嫩',3,1,4.7,150,4100,110,1,NOW(),NOW()),
(4,'樱花日料寿司',4,'北京市朝阳区蓝色港湾',116.463287,39.947500,268.00,'https://picsum.photos/seed/shop4/640/480','["https://picsum.photos/seed/shop4a/640/480"]','11:30-21:30','010-88880004','空运刺身,匠心手握寿司',4,1,4.9,88,2100,55,1,NOW(),NOW()),
(5,'那不勒斯西餐厅',5,'北京市西城区西单大悦城',116.373177,39.907650,198.00,'https://picsum.photos/seed/shop5/640/480','["https://picsum.photos/seed/shop5a/640/480"]','11:00-22:00','010-88880005','手工披萨,黑松露意面',4,1,4.5,66,1600,38,0,NOW(),NOW()),
(6,'炭火烧烤大排档',6,'北京市海淀区五道口',116.337742,39.992730,68.00,'https://picsum.photos/seed/shop6/640/480','["https://picsum.photos/seed/shop6a/640/480"]','17:00-04:00','010-88880006','深夜烧烤,烤串配啤酒',3,1,4.4,210,5200,145,1,NOW(),NOW()),
(7,'甜心烘焙工坊',7,'北京市东城区南锣鼓巷',116.403316,39.937193,45.00,'https://picsum.photos/seed/shop7/640/480','["https://picsum.photos/seed/shop7a/640/480"]','09:00-21:00','010-88880007','手作蛋糕,现烤面包',4,1,4.6,75,1900,42,0,NOW(),NOW()),
(8,'老北京炸酱面',8,'北京市西城区什刹海',116.389487,39.940398,32.00,'https://picsum.photos/seed/shop8/640/480','["https://picsum.photos/seed/shop8a/640/480"]','10:00-21:00','010-88880008','地道京味,炸酱面卤煮',3,1,4.3,180,3600,98,0,NOW(),NOW()),
(9,'湘遇剁椒鱼头',9,'北京市朝阳区望京SOHO',116.481288,39.996250,108.00,'https://picsum.photos/seed/shop9/640/480','["https://picsum.photos/seed/shop9a/640/480"]','10:30-22:00','010-88880009','湖南风味,剁椒鱼头香辣过瘾',4,0,0.0,0,120,0,0,NOW(),NOW());

-- 推荐菜品
INSERT INTO `dish` (`shop_id`,`name`,`price`,`image`,`description`,`sort`,`create_time`) VALUES
(1,'水煮鱼',68.00,'https://picsum.photos/seed/dish1/400/300','招牌水煮鱼,麻辣鲜香',1,NOW()),
(1,'麻婆豆腐',28.00,'https://picsum.photos/seed/dish2/400/300','嫩滑豆腐,麻辣下饭',2,NOW()),
(1,'回锅肉',48.00,'https://picsum.photos/seed/dish3/400/300','肥而不腻',3,NOW()),
(2,'虾饺皇',38.00,'https://picsum.photos/seed/dish4/400/300','晶莹剔透,鲜虾满满',1,NOW()),
(2,'流沙包',22.00,'https://picsum.photos/seed/dish5/400/300','爆浆流沙',2,NOW()),
(3,'鲜毛肚',58.00,'https://picsum.photos/seed/dish6/400/300','七上八下,脆嫩',1,NOW()),
(3,'鸭血',18.00,'https://picsum.photos/seed/dish7/400/300','嫩滑爽口',2,NOW()),
(4,'三文鱼刺身',88.00,'https://picsum.photos/seed/dish8/400/300','空运新鲜',1,NOW()),
(6,'烤羊肉串',6.00,'https://picsum.photos/seed/dish9/400/300','现烤现吃',1,NOW());

-- 菜谱 (status:1发布 0待审核)
INSERT INTO `recipe` (`id`,`title`,`cover`,`author_id`,`category_id`,`cook_time`,`difficulty`,`description`,`status`,`view_count`,`like_count`,`favorite_count`,`recommend`,`create_time`,`update_time`) VALUES
(1,'家常麻婆豆腐','https://picsum.photos/seed/recipe1/640/480',5,1,20,1,'<p>麻辣鲜香的经典川菜,下饭神器!</p>',1,1200,230,180,1,NOW(),NOW()),
(2,'广式白切鸡','https://picsum.photos/seed/recipe2/640/480',6,2,40,2,'<p>皮爽肉滑,原汁原味</p>',1,980,190,150,1,NOW(),NOW()),
(3,'番茄牛腩煲','https://picsum.photos/seed/recipe3/640/480',5,5,90,2,'<p>酸甜开胃,牛腩软烂</p>',1,760,140,110,0,NOW(),NOW()),
(4,'日式照烧鸡腿饭','https://picsum.photos/seed/recipe4/640/480',6,4,30,1,'<p>咸甜照烧汁,米饭杀手</p>',1,1500,320,260,1,NOW(),NOW()),
(5,'提拉米苏','https://picsum.photos/seed/recipe5/640/480',7,7,60,3,'<p>免烤箱,入口即化</p>',1,2100,410,350,1,NOW(),NOW()),
(6,'剁椒鱼头','https://picsum.photos/seed/recipe6/640/480',7,9,45,2,'<p>湖南名菜,鲜辣爽口(待审核示例)</p>',0,60,5,3,0,NOW(),NOW());

-- 菜谱食材
INSERT INTO `recipe_ingredient` (`recipe_id`,`name`,`amount`,`sort`) VALUES
(1,'嫩豆腐','1盒',1),(1,'牛肉末','100g',2),(1,'郫县豆瓣酱','2勺',3),(1,'花椒粉','适量',4),(1,'蒜苗','2根',5),
(2,'三黄鸡','半只',1),(2,'生姜','5片',2),(2,'葱','3根',3),(2,'料酒','1勺',4),
(3,'牛腩','500g',1),(3,'番茄','3个',2),(3,'土豆','2个',3),(3,'洋葱','1个',4),
(4,'鸡腿','2只',1),(4,'照烧汁','3勺',2),(4,'米饭','1碗',3),(4,'西兰花','半颗',4),
(5,'马斯卡彭','250g',1),(5,'手指饼干','1包',2),(5,'咖啡','100ml',3),(5,'可可粉','适量',4),
(6,'鱼头','1个',1),(6,'剁椒','4勺',2),(6,'蒸鱼豉油','2勺',3),(6,'姜蒜','适量',4);

-- 菜谱步骤
INSERT INTO `recipe_step` (`recipe_id`,`step_no`,`image`,`content`,`sort`) VALUES
(1,1,'https://picsum.photos/seed/step1a/400/300','豆腐切块,冷水下锅焯烫去豆腥',1),
(1,2,'https://picsum.photos/seed/step1b/400/300','热油炒香牛肉末与豆瓣酱',2),
(1,3,'https://picsum.photos/seed/step1c/400/300','加水下豆腐焖煮,勾芡撒花椒粉蒜苗',3),
(2,1,'https://picsum.photos/seed/step2a/400/300','整鸡冷水下锅,加姜葱料酒',1),
(2,2,'https://picsum.photos/seed/step2b/400/300','浸煮15分钟后冰水浸凉,斩件摆盘',2),
(3,1,'https://picsum.photos/seed/step3a/400/300','牛腩焯水,番茄切块炒出沙',1),
(3,2,'https://picsum.photos/seed/step3b/400/300','加水与牛腩慢炖1小时,下土豆',2),
(4,1,'https://picsum.photos/seed/step4a/400/300','鸡腿去骨煎至金黄',1),
(4,2,'https://picsum.photos/seed/step4b/400/300','倒入照烧汁收汁,切块盖饭',2),
(5,1,'https://picsum.photos/seed/step5a/400/300','马斯卡彭与蛋黄糊拌匀',1),
(5,2,'https://picsum.photos/seed/step5b/400/300','手指饼干蘸咖啡铺底,分层冷藏',2),
(6,1,'https://picsum.photos/seed/step6a/400/300','鱼头对半,铺剁椒姜蒜',1),
(6,2,'https://picsum.photos/seed/step6b/400/300','大火蒸12分钟,淋豉油热油',2);

-- 探店笔记 (status:1发布 0待审核)
INSERT INTO `note` (`id`,`shop_id`,`author_id`,`title`,`content`,`images`,`rating`,`status`,`like_count`,`comment_count`,`view_count`,`recommend`,`create_time`,`update_time`) VALUES
(1,1,5,'王府井这家川菜yyds!','<p>水煮鱼太赞了,鱼片嫩滑,麻辣够劲!人均88性价比高</p>','["https://picsum.photos/seed/note1a/640/480","https://picsum.photos/seed/note1b/640/480"]',5,1,88,3,560,1,NOW(),NOW()),
(2,2,6,'广式早茶天花板','<p>虾饺皇皮薄馅大,流沙包爆浆,环境优雅</p>','["https://picsum.photos/seed/note2a/640/480"]',5,1,66,2,420,1,NOW(),NOW()),
(3,3,5,'国贸火锅聚会首选','<p>毛肚新鲜,锅底够味,服务贴心</p>','["https://picsum.photos/seed/note3a/640/480"]',4,1,45,1,380,0,NOW(),NOW()),
(4,3,7,'深夜火锅局','<p>营业到凌晨2点,夜宵党福音!</p>','["https://picsum.photos/seed/note4a/640/480"]',5,1,52,0,300,0,NOW(),NOW()),
(5,4,6,'寿司匠心之作','<p>三文鱼入口即化,主厨手握寿司一流</p>','["https://picsum.photos/seed/note5a/640/480"]',5,1,73,1,510,1,NOW(),NOW()),
(6,6,7,'五道口烧烤深夜食堂','<p>烤串滋滋冒油,配冰啤绝了</p>','["https://picsum.photos/seed/note6a/640/480"]',4,1,90,2,680,0,NOW(),NOW()),
(7,8,5,'地道京味炸酱面','<p>面条筋道,炸酱香浓,回忆的味道</p>','["https://picsum.photos/seed/note7a/640/480"]',4,1,38,0,260,0,NOW(),NOW()),
(8,1,6,'再刷老王川菜','<p>回锅肉肥而不腻,麻婆豆腐嫩滑(待审核示例)</p>','["https://picsum.photos/seed/note8a/640/480"]',5,0,0,0,20,0,NOW(),NOW());

-- 评论 (多级: parent_id=0 顶级, 其余为回复)
INSERT INTO `comment` (`id`,`target_type`,`target_id`,`user_id`,`parent_id`,`reply_user_id`,`content`,`like_count`,`create_time`) VALUES
(1,'NOTE',1,6,0,NULL,'看着就流口水,周末去打卡!',5,NOW()),
(2,'NOTE',1,7,1,6,'我上周去过,确实赞!',2,NOW()),
(3,'NOTE',1,5,2,7,'哈哈欢迎组队呀',1,NOW()),
(4,'NOTE',2,5,0,NULL,'虾饺看着好诱人',3,NOW()),
(5,'NOTE',2,7,4,5,'同款喜欢!',0,NOW()),
(6,'RECIPE',1,6,0,NULL,'跟着做成功了,家人都说好吃',8,NOW()),
(7,'RECIPE',1,7,6,6,'求详细火候',1,NOW()),
(8,'RECIPE',5,5,0,NULL,'提拉米苏免烤太方便了',4,NOW());

-- 点赞
INSERT INTO `user_like` (`user_id`,`target_type`,`target_id`,`create_time`) VALUES
(5,'NOTE',2,NOW()),(5,'NOTE',6,NOW()),(5,'RECIPE',4,NOW()),(5,'RECIPE',5,NOW()),
(6,'NOTE',1,NOW()),(6,'RECIPE',1,NOW()),(7,'NOTE',1,NOW()),(7,'RECIPE',5,NOW());

-- 收藏夹
INSERT INTO `favorite_folder` (`id`,`user_id`,`name`,`type`,`create_time`) VALUES
(1,5,'必吃榜','SHOP',NOW()),
(2,5,'周末探店','SHOP',NOW()),
(3,5,'想学的菜','RECIPE',NOW());

-- 收藏
INSERT INTO `user_favorite` (`user_id`,`target_type`,`target_id`,`folder_id`,`create_time`) VALUES
(5,'SHOP',1,1,NOW()),(5,'SHOP',3,1,NOW()),(5,'SHOP',4,2,NOW()),
(5,'RECIPE',4,3,NOW()),(5,'RECIPE',5,3,NOW()),(6,'SHOP',2,0,NOW()),(6,'RECIPE',1,0,NOW());

-- 点亮店铺 (checkin_time 分散近30天, 用于趋势图)
INSERT INTO `shop_checkin` (`user_id`,`shop_id`,`note_id`,`checkin_time`,`create_time`) VALUES
(5,1,1,DATE_SUB(NOW(),INTERVAL 2 DAY),NOW()),
(5,3,3,DATE_SUB(NOW(),INTERVAL 5 DAY),NOW()),
(5,8,7,DATE_SUB(NOW(),INTERVAL 8 DAY),NOW()),
(6,1,8,DATE_SUB(NOW(),INTERVAL 1 DAY),NOW()),
(6,2,2,DATE_SUB(NOW(),INTERVAL 3 DAY),NOW()),
(6,4,5,DATE_SUB(NOW(),INTERVAL 6 DAY),NOW()),
(7,3,4,DATE_SUB(NOW(),INTERVAL 4 DAY),NOW()),
(7,6,6,DATE_SUB(NOW(),INTERVAL 7 DAY),NOW());

-- 预约 (各状态示例)
INSERT INTO `reservation` (`user_id`,`shop_id`,`reserve_time`,`people_count`,`contact_name`,`contact_phone`,`remark`,`status`,`create_time`,`update_time`) VALUES
(5,1,DATE_ADD(NOW(),INTERVAL 1 DAY),4,'小李','13800000005','靠窗位置',0,NOW(),NOW()),
(5,3,DATE_ADD(NOW(),INTERVAL 2 DAY),6,'小李','13800000005','包间',1,NOW(),NOW()),
(6,2,DATE_SUB(NOW(),INTERVAL 3 DAY),2,'Amy','13800000006','',3,NOW(),NOW()),
(7,6,DATE_ADD(NOW(),INTERVAL 1 DAY),8,'本本','13800000007','户外区',0,NOW(),NOW());

-- 复刻晒图
INSERT INTO `recipe_repost` (`recipe_id`,`user_id`,`images`,`content`,`like_count`,`create_time`) VALUES
(1,6,'["https://picsum.photos/seed/repost1/400/300"]','第一次做就成功,麻辣鲜香!',6,NOW()),
(5,7,'["https://picsum.photos/seed/repost2/400/300"]','提拉米苏成品,朋友夸爆',9,NOW()),
(4,5,'["https://picsum.photos/seed/repost3/400/300"]','照烧鸡腿饭光盘了',4,NOW());

-- 食材采购清单 (user1: 0待采购 1家中已有 2已核销)
INSERT INTO `shopping_item` (`user_id`,`name`,`amount`,`recipe_id`,`status`,`create_time`,`update_time`) VALUES
(5,'嫩豆腐','1盒',1,0,NOW(),NOW()),
(5,'牛肉末','100g',1,0,NOW(),NOW()),
(5,'郫县豆瓣酱','2勺',1,1,NOW(),NOW()),
(5,'鸡腿','2只',4,0,NOW(),NOW()),
(5,'照烧汁','3勺',4,1,NOW(),NOW()),
(5,'马斯卡彭','250g',5,2,NOW(),NOW());

-- 聊天会话 & 消息 (user1 <-> merchant1)
INSERT INTO `chat_session` (`id`,`user_id`,`merchant_id`,`shop_id`,`last_message`,`last_time`,`user_unread`,`merchant_unread`,`create_time`,`update_time`) VALUES
(1,5,3,1,'好的,给您留靠窗位置~',NOW(),0,1,NOW(),NOW()),
(2,6,4,2,'请问早茶需要预约吗?',NOW(),1,0,NOW(),NOW());

INSERT INTO `chat_message` (`session_id`,`from_user_id`,`to_user_id`,`content`,`type`,`is_read`,`create_time`) VALUES
(1,5,3,'老板,今晚还有位置吗?','text',1,DATE_SUB(NOW(),INTERVAL 30 MINUTE)),
(1,3,5,'有的,几位呢?','text',1,DATE_SUB(NOW(),INTERVAL 28 MINUTE)),
(1,5,3,'4位,想要靠窗','text',1,DATE_SUB(NOW(),INTERVAL 25 MINUTE)),
(1,3,5,'好的,给您留靠窗位置~','text',0,DATE_SUB(NOW(),INTERVAL 20 MINUTE)),
(2,6,4,'请问早茶需要预约吗?','text',0,DATE_SUB(NOW(),INTERVAL 10 MINUTE));

-- 系统配置
INSERT INTO `sys_config` (`config_key`,`config_value`,`remark`,`update_time`) VALUES
('site_name','觅食记 · 美食探店菜谱平台','站点名称',NOW()),
('upload_max_size','20','上传文件大小限制(MB)',NOW()),
('amap_key','','高德地图Key(演示无需填写)',NOW()),
('search_analyzer','ik_smart','全文搜索分词模式',NOW()),
('home_notice','欢迎来到觅食记,发现身边好味道!','首页公告',NOW());

-- 搜索热词
INSERT INTO `search_keyword` (`keyword`,`count`,`update_time`) VALUES
('火锅',320,NOW()),('川菜',280,NOW()),('提拉米苏',210,NOW()),('烧烤',190,NOW()),
('日料',160,NOW()),('麻婆豆腐',140,NOW()),('早茶',120,NOW()),('照烧鸡腿饭',95,NOW());
