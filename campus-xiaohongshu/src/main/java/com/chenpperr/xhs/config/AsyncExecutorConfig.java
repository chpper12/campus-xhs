package com.chenpperr.xhs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class AsyncExecutorConfig {

    @Bean("feedExecutor")
    public ExecutorService feedExecutor(){
        return new ThreadPoolExecutor(
                8,
                16,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactory(){
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r){
                        return new Thread(r,"feed-executor-thread-"+threadNumber.getAndIncrement());
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
