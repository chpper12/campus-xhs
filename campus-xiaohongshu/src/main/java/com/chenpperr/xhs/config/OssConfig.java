package com.chenpperr.xhs.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类
 *
 * 读取 aliyun.oss 配置并注入 OSS 客户端实例
 * 仅当 upload.type=oss 时生效
 */
@Data
@Configuration
@ConditionalOnProperty(name = "upload.type", havingValue = "oss")
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {

    /**
     * OSS 端点
     */
    private String endpoint;

    /**
     * AccessKey ID
     */
    private String accessKeyId;

    /**
     * AccessKey Secret
     */
    private String accessKeySecret;

    /**
     * Bucket 名称
     */
    private String bucketName;

    /**
     * 访问域名前缀
     */
    private String urlPrefix;

    /**
     * 创建 OSS 客户端 Bean
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
