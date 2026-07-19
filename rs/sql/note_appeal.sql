-- ==========================================================================
-- 补丁: 差评申诉表 note_appeal
-- 适用于早期已导入 foodie.sql (尚未包含该表) 的库, 可安全重复执行
-- 执行: mysql -uroot -p foodie < note_appeal.sql
-- ==========================================================================
USE `foodie`;

CREATE TABLE IF NOT EXISTS `note_appeal` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `note_id`     BIGINT       NOT NULL COMMENT '被申诉的探店笔记',
  `shop_id`     BIGINT       NOT NULL COMMENT '关联店铺',
  `merchant_id` BIGINT       NOT NULL COMMENT '申诉商家(用户id)',
  `reason`      VARCHAR(500)          DEFAULT NULL COMMENT '申诉理由',
  `status`      TINYINT               DEFAULT 0 COMMENT '0待处理 1已受理 2已驳回',
  `reply`       VARCHAR(500)          DEFAULT NULL COMMENT '平台处理回复',
  `create_time` DATETIME              DEFAULT NULL,
  `update_time` DATETIME              DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_merchant` (`merchant_id`),
  KEY `idx_note` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='差评申诉';

-- 差评申诉示例 (merchant1 对本店 shop3 的 note3 评价发起申诉), 幂等插入
INSERT INTO `note_appeal` (`note_id`,`shop_id`,`merchant_id`,`reason`,`status`,`reply`,`create_time`,`update_time`)
SELECT 3,3,3,'该评价存在与事实不符的描述,当日门店服务正常,恳请平台核实处理',0,NULL,NOW(),NOW()
WHERE NOT EXISTS (SELECT 1 FROM `note_appeal`);
