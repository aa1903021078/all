/*
 Navicat Premium Data Transfer

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80041
 Source Host           : localhost:3306
 Source Schema         : foodie

 Target Server Type    : MySQL
 Target Server Version : 80041
 File Encoding         : 65001

 Date: 22/07/2026 21:09:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜系名',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标(emoji)',
  `sort` int(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜系分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '川菜', '🌶️', 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (2, '粤菜', '🦐', 2, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (3, '火锅', '🍲', 3, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (4, '日料', '🍣', 4, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (5, '西餐', '🍝', 5, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (6, '烧烤', '🍢', 6, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (7, '甜品', '🍰', 7, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (8, '小吃', '🍜', 8, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (9, '湘菜', '🌶', 9, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `category` VALUES (10, '咖啡', '☕', 10, '2026-07-21 20:09:47', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `session_id` bigint(0) NOT NULL,
  `from_user_id` bigint(0) NOT NULL,
  `to_user_id` bigint(0) NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text',
  `is_read` tinyint(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session`(`session_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (1, 1, 5, 3, '老板,今晚还有位置吗?', 'text', 1, '2026-07-21 19:39:48');
INSERT INTO `chat_message` VALUES (2, 1, 3, 5, '有的,几位呢?', 'text', 1, '2026-07-21 19:41:48');
INSERT INTO `chat_message` VALUES (3, 1, 5, 3, '4位,想要靠窗', 'text', 1, '2026-07-21 19:44:48');
INSERT INTO `chat_message` VALUES (4, 1, 3, 5, '好的,给您留靠窗位置~', 'text', 1, '2026-07-21 19:49:48');
INSERT INTO `chat_message` VALUES (5, 2, 6, 4, '请问早茶需要预约吗?', 'text', 0, '2026-07-21 19:59:48');
INSERT INTO `chat_message` VALUES (6, 5, 5, 4, '你好', 'TEXT', 0, '2026-07-21 20:31:04');

-- ----------------------------
-- Table structure for chat_session
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '用户',
  `merchant_id` bigint(0) NOT NULL COMMENT '商家',
  `shop_id` bigint(0) NULL DEFAULT 0,
  `last_message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `last_time` datetime(0) NULL DEFAULT NULL,
  `user_unread` int(0) NULL DEFAULT 0,
  `merchant_unread` int(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session`(`user_id`, `merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '聊天会话' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_session
-- ----------------------------
INSERT INTO `chat_session` VALUES (1, 5, 3, 1, '好的,给您留靠窗位置~', '2026-07-21 20:09:48', 0, 1, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `chat_session` VALUES (2, 6, 4, 2, '请问早茶需要预约吗?', '2026-07-21 20:09:48', 1, 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `chat_session` VALUES (5, 5, 4, 2, '你好', '2026-07-21 20:31:04', 0, 1, '2026-07-21 20:30:55', '2026-07-21 20:30:55');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `target_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'NOTE/RECIPE',
  `target_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父评论 0为顶级',
  `reply_user_id` bigint(0) NULL DEFAULT NULL COMMENT '被回复用户',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `like_count` int(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_target`(`target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 'NOTE', 1, 6, 0, NULL, '看着就流口水,周末去打卡!', 5, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (2, 'NOTE', 1, 7, 1, 6, '我上周去过,确实赞!', 2, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (3, 'NOTE', 1, 5, 2, 7, '哈哈欢迎组队呀', 1, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (4, 'NOTE', 2, 5, 0, NULL, '虾饺看着好诱人', 3, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (5, 'NOTE', 2, 7, 4, 5, '同款喜欢!', 0, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (6, 'RECIPE', 1, 6, 0, NULL, '跟着做成功了,家人都说好吃', 8, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (7, 'RECIPE', 1, 7, 6, 6, '求详细火候', 1, '2026-07-21 20:09:47');
INSERT INTO `comment` VALUES (8, 'RECIPE', 5, 5, 0, NULL, '提拉米苏免烤太方便了', 4, '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for dish
-- ----------------------------
DROP TABLE IF EXISTS `dish`;
CREATE TABLE `dish`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(0) NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `price` decimal(10, 2) NULL DEFAULT 0.00,
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop`(`shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推荐菜品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dish
-- ----------------------------
INSERT INTO `dish` VALUES (1, 1, '水煮鱼', 68.00, 'https://picsum.photos/seed/dish1/400/300', '招牌水煮鱼,麻辣鲜香', 1, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (2, 1, '麻婆豆腐', 28.00, 'https://picsum.photos/seed/dish2/400/300', '嫩滑豆腐,麻辣下饭', 2, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (3, 1, '回锅肉', 48.00, 'https://picsum.photos/seed/dish3/400/300', '肥而不腻', 3, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (4, 2, '虾饺皇', 38.00, 'https://picsum.photos/seed/dish4/400/300', '晶莹剔透,鲜虾满满', 1, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (5, 2, '流沙包', 22.00, 'https://picsum.photos/seed/dish5/400/300', '爆浆流沙', 2, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (6, 3, '鲜毛肚', 58.00, 'https://picsum.photos/seed/dish6/400/300', '七上八下,脆嫩', 1, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (7, 3, '鸭血', 18.00, 'https://picsum.photos/seed/dish7/400/300', '嫩滑爽口', 2, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (8, 4, '三文鱼刺身', 88.00, 'https://picsum.photos/seed/dish8/400/300', '空运新鲜', 1, '2026-07-21 20:09:47');
INSERT INTO `dish` VALUES (9, 6, '烤羊肉串', 6.00, 'https://picsum.photos/seed/dish9/400/300', '现烤现吃', 1, '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for favorite_folder
-- ----------------------------
DROP TABLE IF EXISTS `favorite_folder`;
CREATE TABLE `favorite_folder`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏夹名',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SHOP/RECIPE',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_folder`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏夹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of favorite_folder
-- ----------------------------
INSERT INTO `favorite_folder` VALUES (1, 5, '必吃榜', 'SHOP', '2026-07-21 20:09:47');
INSERT INTO `favorite_folder` VALUES (2, 5, '周末探店', 'SHOP', '2026-07-21 20:09:47');
INSERT INTO `favorite_folder` VALUES (3, 5, '想学的菜', 'RECIPE', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for note
-- ----------------------------
DROP TABLE IF EXISTS `note`;
CREATE TABLE `note`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(0) NULL DEFAULT NULL,
  `author_id` bigint(0) NULL DEFAULT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '富文本内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图集 JSON 数组',
  `rating` tinyint(0) NULL DEFAULT 5 COMMENT '作者评分 1-5',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0待审核 1发布 2下架 3违规',
  `like_count` int(0) NULL DEFAULT 0,
  `comment_count` int(0) NULL DEFAULT 0,
  `view_count` int(0) NULL DEFAULT 0,
  `recommend` tinyint(0) NULL DEFAULT 0 COMMENT '首页置顶推荐',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_note`(`shop_id`) USING BTREE,
  INDEX `idx_author_note`(`author_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '探店笔记' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note
-- ----------------------------
INSERT INTO `note` VALUES (1, 1, 5, '王府井这家川菜yyds!', '<p>水煮鱼太赞了,鱼片嫩滑,麻辣够劲!人均88性价比高</p>', '[\"https://picsum.photos/seed/note1a/640/480\",\"https://picsum.photos/seed/note1b/640/480\"]', 5, 1, 88, 3, 560, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (2, 2, 6, '广式早茶天花板', '<p>虾饺皇皮薄馅大,流沙包爆浆,环境优雅</p>', '[\"https://picsum.photos/seed/note2a/640/480\"]', 5, 1, 66, 2, 420, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (3, 3, 5, '国贸火锅聚会首选', '<p>毛肚新鲜,锅底够味,服务贴心</p>', '[\"https://picsum.photos/seed/note3a/640/480\"]', 4, 1, 45, 1, 380, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (4, 3, 7, '深夜火锅局', '<p>营业到凌晨2点,夜宵党福音!</p>', '[\"https://picsum.photos/seed/note4a/640/480\"]', 5, 1, 52, 0, 300, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (5, 4, 6, '寿司匠心之作', '<p>三文鱼入口即化,主厨手握寿司一流</p>', '[\"https://picsum.photos/seed/note5a/640/480\"]', 5, 1, 73, 1, 510, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (6, 6, 7, '五道口烧烤深夜食堂', '<p>烤串滋滋冒油,配冰啤绝了</p>', '[\"https://picsum.photos/seed/note6a/640/480\"]', 4, 1, 90, 2, 680, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (7, 8, 5, '地道京味炸酱面', '<p>面条筋道,炸酱香浓,回忆的味道</p>', '[\"https://picsum.photos/seed/note7a/640/480\"]', 4, 1, 38, 0, 260, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `note` VALUES (8, 1, 6, '再刷老王川菜', '<p>回锅肉肥而不腻,麻婆豆腐嫩滑(待审核示例)</p>', '[\"https://picsum.photos/seed/note8a/640/480\"]', 5, 0, 0, 0, 20, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for note_appeal
-- ----------------------------
DROP TABLE IF EXISTS `note_appeal`;
CREATE TABLE `note_appeal`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `note_id` bigint(0) NOT NULL COMMENT '被申诉的探店笔记',
  `shop_id` bigint(0) NOT NULL COMMENT '关联店铺',
  `merchant_id` bigint(0) NOT NULL COMMENT '申诉商家(用户id)',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '申诉理由',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '0待处理 1已受理 2已驳回',
  `reply` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '平台处理回复',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_merchant`(`merchant_id`) USING BTREE,
  INDEX `idx_note`(`note_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '差评申诉' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of note_appeal
-- ----------------------------
INSERT INTO `note_appeal` VALUES (1, 3, 3, 3, '该评价存在与事实不符的描述,当日门店服务正常,恳请平台核实处理', 0, NULL, '2026-07-21 20:09:48', '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for recipe
-- ----------------------------
DROP TABLE IF EXISTS `recipe`;
CREATE TABLE `recipe`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜谱名',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `author_id` bigint(0) NULL DEFAULT NULL,
  `category_id` bigint(0) NULL DEFAULT NULL COMMENT '菜系',
  `cook_time` int(0) NULL DEFAULT 0 COMMENT '烹饪时长(分钟)',
  `difficulty` tinyint(0) NULL DEFAULT 1 COMMENT '1简单 2中等 3困难',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '富文本简介',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0待审核 1发布 2下架 3违规',
  `view_count` int(0) NULL DEFAULT 0,
  `like_count` int(0) NULL DEFAULT 0,
  `favorite_count` int(0) NULL DEFAULT 0,
  `recommend` tinyint(0) NULL DEFAULT 0 COMMENT '首页推荐',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_author`(`author_id`) USING BTREE,
  INDEX `idx_cate`(`category_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜谱' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe
-- ----------------------------
INSERT INTO `recipe` VALUES (1, '家常麻婆豆腐', 'https://picsum.photos/seed/recipe1/640/480', 5, 1, 20, 1, '<p>麻辣鲜香的经典川菜,下饭神器!</p>', 1, 1200, 230, 180, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `recipe` VALUES (2, '广式白切鸡', 'https://picsum.photos/seed/recipe2/640/480', 6, 2, 40, 2, '<p>皮爽肉滑,原汁原味</p>', 1, 980, 190, 150, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `recipe` VALUES (3, '番茄牛腩煲', 'https://picsum.photos/seed/recipe3/640/480', 5, 5, 90, 2, '<p>酸甜开胃,牛腩软烂</p>', 1, 760, 140, 110, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `recipe` VALUES (4, '日式照烧鸡腿饭', 'https://picsum.photos/seed/recipe4/640/480', 6, 4, 30, 1, '<p>咸甜照烧汁,米饭杀手</p>', 1, 1501, 320, 260, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `recipe` VALUES (5, '提拉米苏', 'https://picsum.photos/seed/recipe5/640/480', 7, 7, 60, 3, '<p>免烤箱,入口即化</p>', 1, 2101, 410, 350, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `recipe` VALUES (6, '剁椒鱼头', 'https://picsum.photos/seed/recipe6/640/480', 7, 9, 45, 2, '<p>湖南名菜,鲜辣爽口(待审核示例)</p>', 0, 60, 5, 3, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for recipe_ingredient
-- ----------------------------
DROP TABLE IF EXISTS `recipe_ingredient`;
CREATE TABLE `recipe_ingredient`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `recipe_id` bigint(0) NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '食材名',
  `amount` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用量',
  `sort` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_recipe`(`recipe_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜谱食材' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_ingredient
-- ----------------------------
INSERT INTO `recipe_ingredient` VALUES (1, 1, '嫩豆腐', '1盒', 1);
INSERT INTO `recipe_ingredient` VALUES (2, 1, '牛肉末', '100g', 2);
INSERT INTO `recipe_ingredient` VALUES (3, 1, '郫县豆瓣酱', '2勺', 3);
INSERT INTO `recipe_ingredient` VALUES (4, 1, '花椒粉', '适量', 4);
INSERT INTO `recipe_ingredient` VALUES (5, 1, '蒜苗', '2根', 5);
INSERT INTO `recipe_ingredient` VALUES (6, 2, '三黄鸡', '半只', 1);
INSERT INTO `recipe_ingredient` VALUES (7, 2, '生姜', '5片', 2);
INSERT INTO `recipe_ingredient` VALUES (8, 2, '葱', '3根', 3);
INSERT INTO `recipe_ingredient` VALUES (9, 2, '料酒', '1勺', 4);
INSERT INTO `recipe_ingredient` VALUES (10, 3, '牛腩', '500g', 1);
INSERT INTO `recipe_ingredient` VALUES (11, 3, '番茄', '3个', 2);
INSERT INTO `recipe_ingredient` VALUES (12, 3, '土豆', '2个', 3);
INSERT INTO `recipe_ingredient` VALUES (13, 3, '洋葱', '1个', 4);
INSERT INTO `recipe_ingredient` VALUES (14, 4, '鸡腿', '2只', 1);
INSERT INTO `recipe_ingredient` VALUES (15, 4, '照烧汁', '3勺', 2);
INSERT INTO `recipe_ingredient` VALUES (16, 4, '米饭', '1碗', 3);
INSERT INTO `recipe_ingredient` VALUES (17, 4, '西兰花', '半颗', 4);
INSERT INTO `recipe_ingredient` VALUES (18, 5, '马斯卡彭', '250g', 1);
INSERT INTO `recipe_ingredient` VALUES (19, 5, '手指饼干', '1包', 2);
INSERT INTO `recipe_ingredient` VALUES (20, 5, '咖啡', '100ml', 3);
INSERT INTO `recipe_ingredient` VALUES (21, 5, '可可粉', '适量', 4);
INSERT INTO `recipe_ingredient` VALUES (22, 6, '鱼头', '1个', 1);
INSERT INTO `recipe_ingredient` VALUES (23, 6, '剁椒', '4勺', 2);
INSERT INTO `recipe_ingredient` VALUES (24, 6, '蒸鱼豉油', '2勺', 3);
INSERT INTO `recipe_ingredient` VALUES (25, 6, '姜蒜', '适量', 4);

-- ----------------------------
-- Table structure for recipe_repost
-- ----------------------------
DROP TABLE IF EXISTS `recipe_repost`;
CREATE TABLE `recipe_repost`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `recipe_id` bigint(0) NOT NULL,
  `user_id` bigint(0) NOT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '成品图 JSON',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '心得',
  `like_count` int(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_recipe_repost`(`recipe_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜谱复刻晒图' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_repost
-- ----------------------------
INSERT INTO `recipe_repost` VALUES (1, 1, 6, '[\"https://picsum.photos/seed/repost1/400/300\"]', '第一次做就成功,麻辣鲜香!', 6, '2026-07-21 20:09:48');
INSERT INTO `recipe_repost` VALUES (2, 5, 7, '[\"https://picsum.photos/seed/repost2/400/300\"]', '提拉米苏成品,朋友夸爆', 9, '2026-07-21 20:09:48');
INSERT INTO `recipe_repost` VALUES (3, 4, 5, '[\"https://picsum.photos/seed/repost3/400/300\"]', '照烧鸡腿饭光盘了', 4, '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for recipe_step
-- ----------------------------
DROP TABLE IF EXISTS `recipe_step`;
CREATE TABLE `recipe_step`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `recipe_id` bigint(0) NOT NULL,
  `step_no` int(0) NULL DEFAULT 1 COMMENT '步骤序号',
  `image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '步骤说明',
  `sort` int(0) NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_recipe_step`(`recipe_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜谱步骤' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recipe_step
-- ----------------------------
INSERT INTO `recipe_step` VALUES (1, 1, 1, 'https://picsum.photos/seed/step1a/400/300', '豆腐切块,冷水下锅焯烫去豆腥', 1);
INSERT INTO `recipe_step` VALUES (2, 1, 2, 'https://picsum.photos/seed/step1b/400/300', '热油炒香牛肉末与豆瓣酱', 2);
INSERT INTO `recipe_step` VALUES (3, 1, 3, 'https://picsum.photos/seed/step1c/400/300', '加水下豆腐焖煮,勾芡撒花椒粉蒜苗', 3);
INSERT INTO `recipe_step` VALUES (4, 2, 1, 'https://picsum.photos/seed/step2a/400/300', '整鸡冷水下锅,加姜葱料酒', 1);
INSERT INTO `recipe_step` VALUES (5, 2, 2, 'https://picsum.photos/seed/step2b/400/300', '浸煮15分钟后冰水浸凉,斩件摆盘', 2);
INSERT INTO `recipe_step` VALUES (6, 3, 1, 'https://picsum.photos/seed/step3a/400/300', '牛腩焯水,番茄切块炒出沙', 1);
INSERT INTO `recipe_step` VALUES (7, 3, 2, 'https://picsum.photos/seed/step3b/400/300', '加水与牛腩慢炖1小时,下土豆', 2);
INSERT INTO `recipe_step` VALUES (8, 4, 1, 'https://picsum.photos/seed/step4a/400/300', '鸡腿去骨煎至金黄', 1);
INSERT INTO `recipe_step` VALUES (9, 4, 2, 'https://picsum.photos/seed/step4b/400/300', '倒入照烧汁收汁,切块盖饭', 2);
INSERT INTO `recipe_step` VALUES (10, 5, 1, 'https://picsum.photos/seed/step5a/400/300', '马斯卡彭与蛋黄糊拌匀', 1);
INSERT INTO `recipe_step` VALUES (11, 5, 2, 'https://picsum.photos/seed/step5b/400/300', '手指饼干蘸咖啡铺底,分层冷藏', 2);
INSERT INTO `recipe_step` VALUES (12, 6, 1, 'https://picsum.photos/seed/step6a/400/300', '鱼头对半,铺剁椒姜蒜', 1);
INSERT INTO `recipe_step` VALUES (13, 6, 2, 'https://picsum.photos/seed/step6b/400/300', '大火蒸12分钟,淋豉油热油', 2);

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `shop_id` bigint(0) NOT NULL,
  `reserve_time` datetime(0) NULL DEFAULT NULL COMMENT '预约到店时间',
  `people_count` int(0) NULL DEFAULT 1,
  `contact_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '0待确认 1已确认 2已拒绝 3已完成 4已取消',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_res`(`user_id`) USING BTREE,
  INDEX `idx_shop_res`(`shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reservation
-- ----------------------------
INSERT INTO `reservation` VALUES (1, 5, 1, '2026-07-22 20:09:48', 4, '小李', '13800000005', '靠窗位置', 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `reservation` VALUES (2, 5, 3, '2026-07-23 20:09:48', 6, '小李', '13800000005', '包间', 1, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `reservation` VALUES (3, 6, 2, '2026-07-18 20:09:48', 2, 'Amy', '13800000006', '', 3, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `reservation` VALUES (4, 7, 6, '2026-07-22 20:09:48', 8, '本本', '13800000007', '户外区', 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for search_keyword
-- ----------------------------
DROP TABLE IF EXISTS `search_keyword`;
CREATE TABLE `search_keyword`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `keyword` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `count` int(0) NULL DEFAULT 1 COMMENT '搜索次数',
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_keyword`(`keyword`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索热词' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_keyword
-- ----------------------------
INSERT INTO `search_keyword` VALUES (1, '火锅', 320, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (2, '川菜', 280, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (3, '提拉米苏', 210, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (4, '烧烤', 190, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (5, '日料', 160, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (6, '麻婆豆腐', 140, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (7, '早茶', 120, '2026-07-21 20:09:48');
INSERT INTO `search_keyword` VALUES (8, '照烧鸡腿饭', 95, '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名',
  `category_id` bigint(0) NULL DEFAULT NULL COMMENT '菜系',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `longitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '经度',
  `latitude` decimal(10, 6) NULL DEFAULT NULL COMMENT '纬度',
  `avg_price` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '人均消费',
  `cover` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '实拍图集 JSON 数组',
  `business_hours` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业时间',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简介',
  `merchant_id` bigint(0) NULL DEFAULT NULL COMMENT '所属商家',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0待审核 1上架 2下架 3拒绝',
  `rating` decimal(2, 1) NULL DEFAULT 0.0 COMMENT '综合评分',
  `rating_count` int(0) NULL DEFAULT 0 COMMENT '评分数',
  `view_count` int(0) NULL DEFAULT 0 COMMENT '曝光/访客',
  `checkin_count` int(0) NULL DEFAULT 0 COMMENT '点亮人数',
  `recommend` tinyint(0) NULL DEFAULT 0 COMMENT '首页推荐',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category_id`) USING BTREE,
  INDEX `idx_merchant`(`merchant_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop
-- ----------------------------
INSERT INTO `shop` VALUES (1, '老王川菜馆', 1, '北京市东城区王府井大街18号', 116.407526, 39.910925, 88.00, 'https://picsum.photos/seed/shop1/640/480', '[\"https://picsum.photos/seed/shop1a/640/480\",\"https://picsum.photos/seed/shop1b/640/480\"]', '10:00-22:00', '010-88880001', '正宗川味,麻辣鲜香,水煮鱼一绝', 3, 1, 4.8, 120, 3202, 86, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (2, '阿婆粤式茶楼', 2, '北京市朝阳区三里屯路11号', 116.447847, 39.937193, 128.00, 'https://picsum.photos/seed/shop2/640/480', '[\"https://picsum.photos/seed/shop2a/640/480\",\"https://picsum.photos/seed/shop2b/640/480\"]', '07:00-15:00', '010-88880002', '老广早茶,虾饺烧卖,一盅两件', 4, 1, 4.6, 98, 2801, 72, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (3, '蜀韵火锅(国贸店)', 3, '北京市朝阳区国贸CBD商圈', 116.457314, 39.921984, 158.00, 'https://picsum.photos/seed/shop3/640/480', '[\"https://picsum.photos/seed/shop3a/640/480\"]', '11:00-02:00', '010-88880003', '麻辣牛油锅底,毛肚鲜嫩', 3, 1, 4.7, 150, 4100, 110, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (4, '樱花日料寿司', 4, '北京市朝阳区蓝色港湾', 116.463287, 39.947500, 268.00, 'https://picsum.photos/seed/shop4/640/480', '[\"https://picsum.photos/seed/shop4a/640/480\"]', '11:30-21:30', '010-88880004', '空运刺身,匠心手握寿司', 4, 1, 4.9, 88, 2100, 55, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (5, '那不勒斯西餐厅', 5, '北京市西城区西单大悦城', 116.373177, 39.907650, 198.00, 'https://picsum.photos/seed/shop5/640/480', '[\"https://picsum.photos/seed/shop5a/640/480\"]', '11:00-22:00', '010-88880005', '手工披萨,黑松露意面', 4, 1, 4.5, 66, 1600, 38, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (6, '炭火烧烤大排档', 6, '北京市海淀区五道口', 116.337742, 39.992730, 68.00, 'https://picsum.photos/seed/shop6/640/480', '[\"https://picsum.photos/seed/shop6a/640/480\"]', '17:00-04:00', '010-88880006', '深夜烧烤,烤串配啤酒', 3, 1, 4.4, 210, 5200, 145, 1, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (7, '甜心烘焙工坊', 7, '北京市东城区南锣鼓巷', 116.403316, 39.937193, 45.00, 'https://picsum.photos/seed/shop7/640/480', '[\"https://picsum.photos/seed/shop7a/640/480\"]', '09:00-21:00', '010-88880007', '手作蛋糕,现烤面包', 4, 1, 4.6, 75, 1900, 42, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (8, '老北京炸酱面', 8, '北京市西城区什刹海', 116.389487, 39.940398, 32.00, 'https://picsum.photos/seed/shop8/640/480', '[\"https://picsum.photos/seed/shop8a/640/480\"]', '10:00-21:00', '010-88880008', '地道京味,炸酱面卤煮', 3, 1, 4.3, 180, 3601, 98, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (9, '湘遇剁椒鱼头', 9, '北京市朝阳区望京SOHO', 116.481288, 39.996250, 108.00, 'https://picsum.photos/seed/shop9/640/480', '[\"https://picsum.photos/seed/shop9a/640/480\"]', '10:30-22:00', '010-88880009', '湖南风味,剁椒鱼头香辣过瘾', 4, 0, 0.0, 0, 120, 0, 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `shop` VALUES (10, '234234', 2, '青岛', 35.000000, 67.000000, 60.00, '/api/upload/20260721/ce9aacccf5e54854acd1d5f45017c5f3.jpg', NULL, NULL, NULL, '32423', NULL, 1, 0.0, 0, 0, 0, 0, '2026-07-21 20:25:39', '2026-07-21 20:25:39');

-- ----------------------------
-- Table structure for shop_checkin
-- ----------------------------
DROP TABLE IF EXISTS `shop_checkin`;
CREATE TABLE `shop_checkin`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `shop_id` bigint(0) NOT NULL,
  `note_id` bigint(0) NULL DEFAULT 0,
  `checkin_time` datetime(0) NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_checkin`(`user_id`, `shop_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点亮店铺' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_checkin
-- ----------------------------
INSERT INTO `shop_checkin` VALUES (1, 5, 1, 1, '2026-07-19 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (2, 5, 3, 3, '2026-07-16 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (3, 5, 8, 7, '2026-07-13 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (4, 6, 1, 8, '2026-07-20 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (5, 6, 2, 2, '2026-07-18 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (6, 6, 4, 5, '2026-07-15 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (7, 7, 3, 4, '2026-07-17 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shop_checkin` VALUES (8, 7, 6, 6, '2026-07-14 20:09:48', '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for shopping_item
-- ----------------------------
DROP TABLE IF EXISTS `shopping_item`;
CREATE TABLE `shopping_item`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '食材名',
  `amount` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用量',
  `recipe_id` bigint(0) NULL DEFAULT 0 COMMENT '来源菜谱',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '0待采购 1家中已有 2已核销',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_shopping`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '食材采购清单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shopping_item
-- ----------------------------
INSERT INTO `shopping_item` VALUES (1, 5, '嫩豆腐', '1盒', 1, 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shopping_item` VALUES (2, 5, '牛肉末', '100g', 1, 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shopping_item` VALUES (3, 5, '郫县豆瓣酱', '2勺', 1, 1, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shopping_item` VALUES (4, 5, '鸡腿', '2只', 4, 0, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shopping_item` VALUES (5, 5, '照烧汁', '3勺', 4, 1, '2026-07-21 20:09:48', '2026-07-21 20:09:48');
INSERT INTO `shopping_item` VALUES (6, 5, '马斯卡彭', '250g', 5, 2, '2026-07-21 20:09:48', '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'site_name', '觅食记 · 美食探店菜谱平台', '站点名称', '2026-07-21 20:09:48');
INSERT INTO `sys_config` VALUES (2, 'upload_max_size', '20', '上传文件大小限制(MB)', '2026-07-21 20:09:48');
INSERT INTO `sys_config` VALUES (3, 'amap_key', '', '高德地图Key(演示无需填写)', '2026-07-21 20:09:48');
INSERT INTO `sys_config` VALUES (4, 'search_analyzer', 'ik_smart', '全文搜索分词模式', '2026-07-21 20:09:48');
INSERT INTO `sys_config` VALUES (5, 'home_notice', '欢迎来到觅食记,发现身边好味道!', '首页公告', '2026-07-21 20:09:48');

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码 如 content:review',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '全部权限', '*:*:*', '超级管理员');
INSERT INTO `sys_permission` VALUES (2, '内容审核', 'content:review', '笔记/菜谱审核');
INSERT INTO `sys_permission` VALUES (3, '内容管理', 'content:manage', '内容下架删除');
INSERT INTO `sys_permission` VALUES (4, '店铺管理', 'shop:manage', '平台店铺管理');
INSERT INTO `sys_permission` VALUES (5, '用户管理', 'user:manage', '用户封禁');
INSERT INTO `sys_permission` VALUES (6, '运营管理', 'operation:manage', '推荐位配置');
INSERT INTO `sys_permission` VALUES (7, '系统配置', 'config:manage', '系统参数/RBAC');
INSERT INTO `sys_permission` VALUES (8, '数据大屏', 'dashboard:view', '平台数据看板');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'ADMIN', '拥有全部权限', '2026-07-21 20:09:47');
INSERT INTO `sys_role` VALUES (2, '审核员', 'REVIEWER', '仅内容审核相关权限', '2026-07-21 20:09:47');
INSERT INTO `sys_role` VALUES (3, '商家', 'MERCHANT', '商家端权限', '2026-07-21 20:09:47');
INSERT INTO `sys_role` VALUES (4, '普通用户', 'USER', '用户端权限', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(0) NOT NULL,
  `permission_id` bigint(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1);
INSERT INTO `sys_role_permission` VALUES (2, 2, 2);
INSERT INTO `sys_role_permission` VALUES (3, 2, 3);
INSERT INTO `sys_role_permission` VALUES (4, 2, 8);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BCrypt 密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` tinyint(0) NULL DEFAULT 0 COMMENT '0未知 1男 2女',
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '0正常 1封禁',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '超级管理员', 'https://picsum.photos/seed/avatar1/100/100', '13800000001', 'admin@foodie.com', 1, '平台超级管理员', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (2, 'reviewer', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '内容审核员', 'https://picsum.photos/seed/avatar2/100/100', '13800000002', 'review@foodie.com', 2, '负责内容审核', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (3, 'merchant1', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '老王川菜', 'https://picsum.photos/seed/avatar3/100/100', '13800000003', 'wang@foodie.com', 1, '正宗川味 麻辣鲜香', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (4, 'merchant2', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '阿婆粤茶', 'https://picsum.photos/seed/avatar4/100/100', '13800000004', 'po@foodie.com', 2, '老广早茶 一盅两件', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (5, 'user1', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '吃货小李', 'https://picsum.photos/seed/avatar5/100/100', '13800000005', 'li@foodie.com', 1, '用美食记录生活', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (6, 'user2', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '美食家Amy', 'https://picsum.photos/seed/avatar6/100/100', '13800000006', 'amy@foodie.com', 2, '探店达人 打卡100家', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');
INSERT INTO `sys_user` VALUES (7, 'user3', '$2a$10$..IQV5s1j5CS7isPetl.XeEXKCOSFFiaKbphN8No.5PYQwyWGWUqq', '深夜食堂', 'https://picsum.photos/seed/avatar7/100/100', '13800000007', 'ben@foodie.com', 1, '专注夜宵三十年', 0, '2026-07-21 20:09:47', '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `role_id` bigint(0) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户-角色' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2, 2);
INSERT INTO `sys_user_role` VALUES (3, 3, 3);
INSERT INTO `sys_user_role` VALUES (4, 3, 4);
INSERT INTO `sys_user_role` VALUES (5, 4, 3);
INSERT INTO `sys_user_role` VALUES (6, 4, 4);
INSERT INTO `sys_user_role` VALUES (7, 5, 4);
INSERT INTO `sys_user_role` VALUES (8, 6, 4);
INSERT INTO `sys_user_role` VALUES (9, 7, 4);

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `target_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SHOP/RECIPE',
  `target_id` bigint(0) NOT NULL,
  `folder_id` bigint(0) NULL DEFAULT 0,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_fav`(`user_id`, `target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_favorite
-- ----------------------------
INSERT INTO `user_favorite` VALUES (1, 5, 'SHOP', 1, 1, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (2, 5, 'SHOP', 3, 1, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (3, 5, 'SHOP', 4, 2, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (4, 5, 'RECIPE', 4, 3, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (5, 5, 'RECIPE', 5, 3, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (6, 6, 'SHOP', 2, 0, '2026-07-21 20:09:47');
INSERT INTO `user_favorite` VALUES (7, 6, 'RECIPE', 1, 0, '2026-07-21 20:09:47');

-- ----------------------------
-- Table structure for user_like
-- ----------------------------
DROP TABLE IF EXISTS `user_like`;
CREATE TABLE `user_like`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL,
  `target_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'NOTE/RECIPE/COMMENT/REPOST',
  `target_id` bigint(0) NOT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_like`(`user_id`, `target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_like
-- ----------------------------
INSERT INTO `user_like` VALUES (1, 5, 'NOTE', 2, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (2, 5, 'NOTE', 6, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (3, 5, 'RECIPE', 4, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (4, 5, 'RECIPE', 5, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (5, 6, 'NOTE', 1, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (6, 6, 'RECIPE', 1, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (7, 7, 'NOTE', 1, '2026-07-21 20:09:47');
INSERT INTO `user_like` VALUES (8, 7, 'RECIPE', 5, '2026-07-21 20:09:47');

SET FOREIGN_KEY_CHECKS = 1;
