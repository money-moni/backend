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
    @Bean(name = "customExecutorFeign")
    public Executor myCustomExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(600); // 지속적으로 유지할 스레드
        executor.setMaxPoolSize(1000);  // 피크 대응
        executor.setQueueCapacity(2000); // 큐에 쌓일 수 있는 요청 수
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("customFeign-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "customExecutorWebClient")
    public Executor webClientExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(300);           // WebClient는 주로 I/O 콜백 처리 → 적절한 수준이면 충분
        executor.setMaxPoolSize(600);            // 병렬 요청량 많을 경우 대비
        executor.setQueueCapacity(1000);         // 대기열 용량
        executor.setThreadNamePrefix("customWebClient-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
