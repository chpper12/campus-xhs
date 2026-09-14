package com.chenpperr.xhs.market.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MarketOrderCreateDTO {

    @NotNull(message = "商品ID不能为空")
    @Positive(message = "商品ID不合法")
    private Long itemId;

}
