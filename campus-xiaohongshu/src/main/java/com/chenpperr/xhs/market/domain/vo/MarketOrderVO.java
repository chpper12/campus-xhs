package com.chenpperr.xhs.market.domain.vo;

import com.chenpperr.xhs.domain.vo.UserSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 市集订单视图对象（用于「我的订单」列表展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketOrderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 唯一订单号
     */
    private String orderSn;

    /**
     * 商品ID
     */
    private Long itemId;

    /**
     * 商品标题（商品被删除时返回「商品已删除」）
     */
    private String itemTitle;

    /**
     * 商品封面图URL
     */
    private String itemCoverUrl;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 订单状态: 0-待支付, 1-已完成(已支付), 2-已取消(超时/手动)
     */
    private Integer status;

    /**
     * 交易对方信息（买家视角=卖家，卖家视角=买家）
     */
    private UserSimpleVO counterparty;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
