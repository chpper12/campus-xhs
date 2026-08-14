package com.chenpperr.xhs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI大模型配置类
 *
 * 支持兼容OpenAI格式的各大模型API：
 * - OpenAI
 * - 通义千问（阿里）
 * - DeepSeek
 * - 智谱GLM
 * - 其他兼容接口
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiConfig {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 最大token数
     */
    private Integer maxTokens = 500;

    /**
     * 温度（0-1，越高越随机）
     */
    private Double temperature = 0.7;
}
