package com.chenpperr.xhs;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 校园小红书启动类
 */
@SpringBootApplication
@MapperScan("com.chenpperr.xhs.mapper")
@EnableAsync
@EnableScheduling
public class CampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusApplication.class, args);
    }
}
