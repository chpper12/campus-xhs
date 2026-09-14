package com.chenpperr.xhs.market.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("market_item")
public class MarketItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sellerId;

    private String title;

    private String description;

    private BigDecimal price;

    private String coverUrl;

    private String imageUrls;

    private String contactInfo;

    /**
     * 状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
//-- =====================================================
//        -- 7. 二手商品表
//-- =====================================================
//DROP TABLE IF EXISTS `market_item`;
//CREATE TABLE `market_item` (
//        `id`           BIGINT         NOT NULL AUTO_INCREMENT COMMENT '商品ID',
//        `seller_id`    BIGINT         NOT NULL COMMENT '卖家用户ID(关联 user.id)',
//        `title`        VARCHAR(100)   NOT NULL COMMENT '商品标题',
//        `description`  TEXT           DEFAULT NULL COMMENT '商品详细描述',
//        `price`        DECIMAL(10,2)  NOT NULL COMMENT '价格',
//        `cover_url`    VARCHAR(500)   NOT NULL DEFAULT '' COMMENT '封面图片URL',
//        `image_urls`   TEXT           DEFAULT NULL COMMENT '详情图片URL列表，JSON数组格式',
//        `contact_info` VARCHAR(100)   NOT NULL DEFAULT '' COMMENT '卖家联系方式(微信/电话)',
//        `status`       TINYINT        NOT NULL DEFAULT 0 COMMENT '状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架',
//        `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
//        `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
//PRIMARY KEY (`id`),
//KEY `idx_seller_id` (`seller_id`),
//KEY `idx_status` (`status`),
//KEY `idx_create_time` (`create_time` DESC)
//        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园市集商品表';
