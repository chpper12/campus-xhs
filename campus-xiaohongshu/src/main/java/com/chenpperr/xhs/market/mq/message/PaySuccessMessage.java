package com.chenpperr.xhs.market.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaySuccessMessage implements Serializable {

    private String orderSn;

    private Long buyerId;

    private Long sellerId;

    private Long itemId;
}
