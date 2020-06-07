package com.github.cimsbioko.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

import static java.lang.Thread.MIN_PRIORITY;
import static java.lang.Thread.NORM_PRIORITY;

@Configuration
@EnableAsync
public class AsyncConfig extends AsyncConfigurerSupport {

    @PostConstruct
    void setupSecurityPropagation() {
        // propagates security context from calling thread for async calls, otherwise principal is null
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setQueueCapacity(25);
        executor.initialize();
        executor.setThreadPriority(MIN_PRIORITY + (NORM_PRIORITY - MIN_PRIORITY) / 2);
        return executor;
    }
}
