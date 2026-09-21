package com.chenpperr.xhs.market.mq.consumer;

import com.chenpperr.xhs.market.config.RabbitMQConfig;
import com.chenpperr.xhs.market.service.MarketOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderDelayMessageConsumer {

    private final MarketOrderService marketOrderService;

    private final RedissonClient redissonClient;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = RabbitMQConfig.DELAY_QUEUE, durable = "true"),
            exchange = @Exchange(
                    name = RabbitMQConfig.DELAY_EXCHANGE,
                    type = "x-delayed-message",
                    arguments = @Argument(name = "x-delayed-type", value = "direct")
            ),
            key = RabbitMQConfig.DELAY_ROUTING_KEY
    ))
    public void handleDelayMessage(String orderSn) {
        log.info("收到订单超时检查消息，订单号：{}", orderSn);

        //构造针对当前订单的分布式锁 Key
        String lockKey = "lock:order:" + orderSn;
        RLock lock  = redissonClient.getLock(lockKey);

        try{
            //尝试获取锁：加锁等待时间 5s，锁自动过期时间 10s
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if(isLocked){
                try{
                    //执行具体的超时取消业务
                    marketOrderService.cancelOrderOnTimeOut(orderSn);
                } finally{
                    //确保释放当前线程持有的锁
                    if(lock.isHeldByCurrentThread()){
                        lock.unlock();
                    }
                }
            }else{
                log.warn("获取订单失败，可能正在处理支付/其他取消操作，订单号：{}", orderSn);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("加锁处理异常，订单号：{}", orderSn);
        }

    }
}















