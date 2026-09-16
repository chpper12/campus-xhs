package com.chenpperr.xhs.market.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    //声明延迟交换机、队列、RoutingKey
    public static final String DELAY_EXCHANGE = "order.delay.exchange";
    public static final String DELAY_QUEUE = "order.delay.queue";
    public static final String DELAY_ROUTING_KEY = "order.delay.key";

    //声明延迟交换机
    @Bean
    public CustomExchange delayExchange() {
        Map<String, Object> args = new HashMap<>();
        //声明底层路由模式
        args.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE,"x-delayed-message",true,false,args);
    }

    @Bean
    public Queue delayQueue() {
        return new Queue(DELAY_QUEUE,true);
    }

    @Bean
    public Binding delayBinding() {
        return BindingBuilder.bind(delayQueue())
                .to(delayExchange())
                .with(DELAY_ROUTING_KEY)
                .noargs();
    }

}
