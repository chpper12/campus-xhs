package com.chenpperr.xhs.market.domain.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发布商品请求参数（标题、描述、价格、封面图、联系方式）。
 */
@Data
public class MarketItemPublishDTO {

    @NotBlank(message = "商品标题不能为空")
    @Size(max = 100, message = "商品标题长度不能超过100字")
    private String title;

    private String description;

    @NotNull(message = "商品价格不能为空")
    @Positive(message = "价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确（最多8位整数、2位小数）")
    private BigDecimal price;

    private String coverUrl;

    private String imageUrls;

    @NotBlank(message = "联系方式不能为空")
    @Size(max = 100, message = "联系方式长度不能超过100字")
    private String contactInfo;

}
