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

    //声明支付成功交换机、队列、RoutingKey
    public static final String PAY_EXCHANGE = "order.pay.exchange";
    public static final String PAY_QUEUE = "order.pay.queue";
    public static final String PAY_ROUTING_KEY = "order.pay.key";


}
