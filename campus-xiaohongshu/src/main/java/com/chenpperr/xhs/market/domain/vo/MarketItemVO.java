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
 * 市集商品视图对象（用于商品列表/详情展示）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品详细描述
     */
    private String description;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 封面图片URL
     */
    private String coverUrl;

    /**
     * 详情图片URL列表，JSON数组格式
     */
    private String imageUrls;

    /**
     * 卖家联系方式(微信/电话)
     */
    private String contactInfo;

    /**
     * 状态: 0-待售, 1-锁定中(已下单未支付), 2-已售出, 3-已下架
     */
    private Integer status;

    /**
     * 卖家信息
     */
    private UserSimpleVO seller;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
