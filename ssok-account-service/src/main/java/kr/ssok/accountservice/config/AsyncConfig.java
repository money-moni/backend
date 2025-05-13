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
    @Bean(name = "customExecutor")
    public Executor myCustomExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(200); // 지속적으로 유지할 스레드
        executor.setMaxPoolSize(500);  // 피크 대응
        executor.setQueueCapacity(1000); // 큐에 쌓일 수 있는 요청 수
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("customAsync-");
        executor.initialize();
        return executor;
    }
}
