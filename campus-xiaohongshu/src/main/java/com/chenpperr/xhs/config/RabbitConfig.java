package com.chenpperr.xhs.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitConfig {
    @Bean
    public MessageConverter messageConverter() {
        // 配置 Jackson 消息转换器，将对象转化为 JSON 字符串传输
        return new Jackson2JsonMessageConverter();
    }
}
