package com.chenpperr.xhs.market.listener;

import com.chenpperr.xhs.market.config.RabbitMQConfig;
import com.chenpperr.xhs.market.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDelayMessageListener {

    private final MarketOrderService marketOrderService;

    @RabbitListener(queues = RabbitMQConfig.DELAY_QUEUE)
    public void handleDelayMessage(String orderSn) {
        //接受延迟消息
        marketOrderService.cancelOrderOnTimeOut(orderSn);
    }
}
