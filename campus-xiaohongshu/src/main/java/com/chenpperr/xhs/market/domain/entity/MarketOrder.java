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
@TableName("market_order")
public class MarketOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    //'唯一订单号',
    private String orderSn;

    private Long itemId;

    private Long buyerId;

    private Long sellerId;

    private BigDecimal amount;

    /**
     * 订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动), 3-支付中
     */
    private Integer status;

    private LocalDateTime payTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer refunded;
}


//-- =====================================================
//        -- 8. 订单表
//-- =====================================================
//DROP TABLE IF EXISTS `market_order`;
//CREATE TABLE `market_order` (
//        `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '订单ID',
//        `order_sn`    VARCHAR(64)   NOT NULL COMMENT '唯一订单号',
//        `item_id`     BIGINT        NOT NULL COMMENT '商品ID(关联 market_item.id)',
//        `buyer_id`    BIGINT        NOT NULL COMMENT '买家用户ID(关联 user.id)',
//        `seller_id`   BIGINT        NOT NULL COMMENT '卖家用户ID(关联 user.id)',
//        `amount`      DECIMAL(10,2) NOT NULL COMMENT '交易金额',
//        `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动)',
//        `pay_time`    DATETIME      DEFAULT NULL COMMENT '支付时间',
//        `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
//        `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
//PRIMARY KEY (`id`),
//UNIQUE KEY `uk_order_sn` (`order_sn`),
//KEY `idx_buyer_id` (`buyer_id`),
//KEY `idx_seller_id` (`seller_id`),
//KEY `idx_item_id` (`item_id`),
//KEY `idx_create_time` (`create_time` DESC)
//        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校园市集订单表';

