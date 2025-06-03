package kr.ssok.accountservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "customExecutorWebClient")
    public Executor webClientExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();

        executor.setCorePoolSize(cores * 10);       // 기본 스레드 수
        executor.setMaxPoolSize(cores * 20);        // 최대 스레드 수
        executor.setQueueCapacity(500);             // 대기열 용량
        executor.setKeepAliveSeconds(120);          // 임시로 늘어난 스레드 유지 시간
        executor.setThreadNamePrefix("customWebClient-");   // 스레드 이름 접두어
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 대기열이 꽉 찬 경우, 서블릿 스레드가 직접 처리

        executor.initialize();
        return executor;
    }
}
