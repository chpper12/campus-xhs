package com.chenpperr.xhs.market.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chenpperr.xhs.domain.entity.User;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.mapper.UserMapper;
import com.chenpperr.xhs.market.config.RabbitMQConfig;
import com.chenpperr.xhs.market.domain.entity.MarketItem;
import com.chenpperr.xhs.market.domain.entity.MarketOrder;
import com.chenpperr.xhs.market.mapper.MarketItemMapper;
import com.chenpperr.xhs.market.mapper.MarketOrderMapper;
import com.chenpperr.xhs.market.mq.message.PaySuccessMessage;
import com.chenpperr.xhs.market.service.MarketItemService;
import com.chenpperr.xhs.market.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaySuccessConsumer {

    private final UserMapper userMapper;

    private final MarketItemMapper marketItemMapper;

    private final MarketOrderService marketOrderService;
    private final RedissonClient redissonClient;
    private final MarketItemService marketItemService;

    //开启事务修改：用户表、订单表、商品表
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConfig.PAY_QUEUE, durable = "true"),
            exchange = @Exchange(name = RabbitMQConfig.PAY_EXCHANGE, type = "topic"),
            key = RabbitMQConfig.PAY_ROUTING_KEY
    ))
    public void handlePaySuccessMessage(PaySuccessMessage msg) throws InterruptedException {

        RLock lock = redissonClient.getLock("lock:order:" + msg.getOrderSn());

        if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
            try {
                marketOrderService.handlePaySuccess(msg);
            } finally {
                if (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }else{
            //这里是没抢到锁的逻辑，也就是抛出异常，rabbitmq会收到nack并“重试”
            throw new IllegalStateException("获取订单失败，等待重试...");
        }


    }

}

