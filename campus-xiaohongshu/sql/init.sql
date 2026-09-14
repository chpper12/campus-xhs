-- =====================================================
-- 校园小红书 MVP - 数据库初始化脚本
-- 数据库名: campus_xiaohongshu
-- 字符集: utf8mb4
-- =====================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `campus_xiaohongshu` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `campus_xiaohongshu`;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
    `password`    VARCHAR(100) NOT NULL COMMENT '登录密码',
    `nickname`    VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`      VARCHAR(500) NOT NULL DEFAULT '' COMMENT '头像URL',
    `bio`         VARCHAR(200) NOT NULL DEFAULT '' COMMENT '个人简介',
    `phone`       VARCHAR(20)  NOT NULL DEFAULT '' COMMENT '手机号',
    `email`       Varchar(100) Not Null DEFAULT '' COMMENT '邮箱',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `balance`     DECIMAL(10,2) NOT NULL DEFAULT 10000.00 COMMENT '模拟钱包余额',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 笔记表
-- =====================================================
DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
    `user_id`          BIGINT        NOT NULL COMMENT '作者ID',
    `title`            VARCHAR(100)  NOT NULL COMMENT '笔记标题',
    `category`         VARCHAR(20)   NOT NULL DEFAULT '推荐' COMMENT '分区：推荐/学习/生活/职业/美食/运动/穿搭/数码/美妆/游戏/娱乐/情感/宠物/兴趣/活动/求助/吐槽',
    `content`          TEXT          NOT NULL COMMENT '用户原始内容',
    `polished_content` TEXT          DEFAULT NULL COMMENT 'AI润色后的内容',
    `tags`             VARCHAR(500)  DEFAULT NULL COMMENT 'AI提取的标签，JSON数组格式',
    `image_urls`       TEXT          DEFAULT NULL COMMENT '图片URL列表，JSON数组格式',
    `like_count`       INT           NOT NULL DEFAULT 0 COMMENT '点赞数（冗余字段）',
    `comment_count`    INT           NOT NULL DEFAULT 0 COMMENT '评论数（冗余字段）',
    `status`           TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：0-草稿 1-已发布 2-已删除',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category` (`category`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- =====================================================
-- 3. 点赞记录表
-- =====================================================
DROP TABLE IF EXISTS `post_like`;
CREATE TABLE `post_like` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `post_id`     BIGINT   NOT NULL COMMENT '笔记ID',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表（持久化备份）';

-- =====================================================
-- 4. 评论表
-- =====================================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id`     BIGINT       NOT NULL COMMENT '笔记ID',
    `user_id`     BIGINT       NOT NULL COMMENT '评论者用户ID',
    `content`     VARCHAR(500) NOT NULL COMMENT '评论内容',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_post_id` (`post_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一级评论表';

-- =====================================================
-- 5. 关注关系表
-- =====================================================
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT   NOT NULL COMMENT '关注者ID（谁关注的）',
    `follow_user_id`  BIGINT   NOT NULL COMMENT '被关注者ID（关注了谁）',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_follow` (`user_id`, `follow_user_id`),
    KEY `idx_follow_user_id` (`follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注关系表';

-- =====================================================
-- 6. 通知表
-- =====================================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `to_user_id`  BIGINT       NOT NULL COMMENT '接收者ID（被通知的人）',
    `from_user_id` BIGINT      NOT NULL COMMENT '触发者ID（点赞/评论的人）',
    `type`        VARCHAR(20)  NOT NULL COMMENT '通知类型：like-点赞 comment-评论 follow-关注',
    `content`     VARCHAR(500) NOT NULL COMMENT '通知文案',
    `post_id`     BIGINT       DEFAULT NULL COMMENT '关联的笔记ID',
    `is_read`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_to_user_id` (`to_user_id`),
    KEY `idx_is_read` (`is_read`),
    KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';


-- =====================================================
-- 7. 二手商品表
-- =====================================================
DROP TABLE IF EXISTS `market_item`;
CREATE TABLE `market_item` (
                               `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
                               `seller_id`    BIGINT         NOT NULL COMMENT '卖家用户ID(关联 user.id)',
                               `title`        VARCHAR(100)   NOT NULL COMMENT '商品标题',
                               `description`  TEXT           DEFAULT NULL COMMENT '商品详细描述',
                               `price`        DECIMAL(10,2)  NOT NULL COMMENT '价格',
                               `cover_url`    VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '封面图片URL',
                               `image_urls`   TEXT           DEFAULT NULL COMMENT '详情图片URL列表，JSON数组格式',
                               `contact_info` VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '卖家联系方式(微信/电话)',
                               `status`       TINYINT        NOT NULL DEFAULT 0 COMMENT '状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架',
                               `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_seller_id` (`seller_id`),
                               KEY `idx_status` (`status`),
                               KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园市集商品表';

-- =====================================================
-- 8. 订单表
-- =====================================================
DROP TABLE IF EXISTS `market_order`;
CREATE TABLE `market_order` (
                                `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
                                `order_sn`    VARCHAR(64)   NOT NULL COMMENT '唯一订单号',
                                `item_id`     BIGINT        NOT NULL COMMENT '商品ID(关联 market_item.id)',
                                `buyer_id`    BIGINT        NOT NULL COMMENT '买家用户ID(关联 user.id)',
                                `seller_id`   BIGINT        NOT NULL COMMENT '卖家用户ID(关联 user.id)',
                                `amount`      DECIMAL(10,2) NOT NULL COMMENT '交易金额',
                                `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动)',
                                `pay_time`    DATETIME      DEFAULT NULL COMMENT '支付时间',
                                `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `uk_order_sn` (`order_sn`),
                                KEY `idx_buyer_id` (`buyer_id`),
                                KEY `idx_seller_id` (`seller_id`),
                                KEY `idx_item_id` (`item_id`),
                                KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园市集订单表';


-- =====================================================
-- 预置测试数据
-- 说明：
--   1. 仅预置 6 个测试用户（初始密码均为 123456，BCrypt 加密存储）
--   2. 帖子/评论/点赞/关注/通知等业务数据不再预置，
--      由用户登录系统后手动操作产生，保证数据天然真实一致
-- =====================================================

-- 用户数据（id: 1~6，头像为空，前端展示默认占位图；后续接入 OSS 后可上传真实头像）
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `bio`, `phone`, `email`) VALUES
(1, 'test', '$2a$10$Zuhr9bmp3RTmJMIC93Jaeu.WQ1TTg1RY9HjVo/TMjZupz1UZNx3Uq',
 '测试用户', '', '热爱编程，分享校园生活~', '13812346789', 'test@qq.com'),
(2, 'test2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
 '测试用户2', '', '美食探店达人 | 干饭不积极思想有问题', '13922334455', 'test2@163.com'),
(3, 'test3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
 '测试用户3', '', '计算机学院大三 | 健身 篮球 夜跑爱好者', '13633445566', 'test3@gmail.com'),
(4, 'test4', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
 '测试用户4', '', '泡图书馆的考研人 | 分享穿搭和学习日常', '13744556677', 'test4@qq.com'),
(5, 'test5', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
 '测试用户5', '', '游戏区常驻玩家 | 开黑滴滴我', '13855667788', 'test5@126.com'),
(6, 'test6', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
 '测试用户6', '', '大一新生 | 正在努力熟悉校园中~', '13966778899', 'test6@qq.com');

-- =====================================================
-- 完成！
-- =====================================================
SELECT '数据库初始化完成！' AS message;
