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
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
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
    `category`         VARCHAR(20)   NOT NULL DEFAULT '推荐' COMMENT '分区：推荐/穿搭/美食/职场/情感/家居/游戏/旅行/健身/视频',
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
-- 预置测试数据
-- =====================================================

-- 用户数据
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`) VALUES
(1, 'test', '123456', '测试用户', 'https://via.placeholder.com/100');

-- 笔记数据
INSERT INTO `post` (`user_id`, `title`, `category`, `content`, `polished_content`, `tags`, `image_urls`, `like_count`, `comment_count`) VALUES
(1, '食堂新品红烧肉测评', '美食', '今天去食堂发现了一个超好吃的窗口，红烧肉特别香，强烈推荐！', '今天漫步校园食堂，意外发现了一个令人惊喜的美食窗口。那里的红烧肉色泽红润，香气四溢，让人垂涎欲滴。强烈推荐给各位同学！', '["#食堂","#美食","#红烧肉","#校园生活"]', '["https://via.placeholder.com/400x300?text=food1"]', 42, 3),
(1, '图书馆自习打卡', '推荐', '图书馆五楼靠窗位置太棒了，阳光正好，学习效率翻倍！', '图书馆五楼靠窗的位置简直是学习的绝佳圣地。温暖的阳光洒在书桌上，让人心情愉悦，学习效率也随之翻倍。强烈推荐！', '["#图书馆","#自习","#学习","#校园生活"]', '["https://via.placeholder.com/400x300?text=library1"]', 28, 1),
(1, '今日穿搭分享', '穿搭', '今天穿了新买的卫衣，搭配牛仔裤，简约又好看～', '今日穿搭分享：一件新入手的卫衣，搭配经典牛仔裤，简约而不失时尚感，轻松打造休闲校园风。', '["#穿搭","#卫衣","#校园风","#日常穿搭"]', '["https://via.placeholder.com/400x300?text=outfit1"]', 35, 2),
(1, '操场夜跑打卡', '健身', '今晚跑了5公里，出汗的感觉真舒服！坚持锻炼，保持好身材。', '今晚在操场完成了5公里夜跑，大汗淋漓的感觉真是畅快淋漓！坚持锻炼，保持健康好身材，一起动起来吧！', '["#健身","#夜跑","#运动","#校园生活"]', '["https://via.placeholder.com/400x300?text=run1"]', 19, 0),
(1, '宿舍游戏开黑', '游戏', '周末和室友一起开黑打游戏，太快乐了！', '周末时光，和室友们一起开黑打游戏，欢声笑语中度过了一段快乐的时光。游戏虽好，也要注意休息哦！', '["#游戏","#室友","#周末","#开黑"]', '["https://via.placeholder.com/400x300?text=game1"]', 23, 1);

-- 评论数据
INSERT INTO `comment` (`post_id`, `user_id`, `content`) VALUES
(1, 1, '红烧肉yyds！'),
(1, 3, '这个窗口在哪里呀？求具体位置！'),
(1, 2, '看起来好好吃！明天也去尝尝～'),
(2, 2, '五楼确实安静，推荐！'),
(3, 1, '好看！求链接～'),
(3, 3, '简约风太赞了！'),
(5, 2, '什么游戏？带我一个！');

-- =====================================================
-- 完成！
-- =====================================================
SELECT '数据库初始化完成！' AS message;
