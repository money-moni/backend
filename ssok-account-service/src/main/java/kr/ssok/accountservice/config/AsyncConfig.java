package kr.ssok.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기 작업 처리를 위한 스레드풀(TaskExecutor) 설정 클래스
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * 논리 코어당 기본 스레드 수 배수
     * <p>application.yaml에 값이 없으면 10을 기본값으로 사용</p>
     */
    @Value("${executor.corePoolSizeMultiplier:10}")
    private int corePoolSizeMultiplierProperty;
    /**
     * 논리 코어당 최대 스레드 수 배수
     * <p>application.yaml에 값이 없으면 20이 기본값으로 사용</p>
     */
    @Value("${executor.maxPoolSizeMultiplier:20}")
    private int maxPoolSizeMultiplierProperty;

    /**
     * WebClient에서 비동기 작업용으로 사용할 커스텀 Executor Bean을 생성
     *
     * <p>
     * - 기본 스레드 수: CPU 코어 수 × corePoolSizeMultiplier<br>
     * - 최대 스레드 수: CPU 코어 수 × maxPoolSizeMultiplier<br>
     * - 대기열 용량: 500<br>
     * - 임시 스레드 유지 시간: 120초<br>
     * - 스레드 이름 접두사: customWebClient-<br>
     * - 대기열 초과시 CallerRunsPolicy(요청 스레드가 직접 처리)
     * </p>
     *
     * @return 비동기 작업을 위한 커스텀 Executor 인스턴스
     */
    @Bean(name = "customExecutorWebClient")
    public Executor webClientExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int cores = Runtime.getRuntime().availableProcessors();
        int corePoolSizeMultiplier = corePoolSizeMultiplierProperty;
        int maxPoolSizeMultiplier = maxPoolSizeMultiplierProperty;

        executor.setCorePoolSize(cores * corePoolSizeMultiplier);       // 기본 스레드 수
        executor.setMaxPoolSize(cores * maxPoolSizeMultiplier);        // 최대 스레드 수
        executor.setQueueCapacity(500);             // 대기열 용량
        executor.setKeepAliveSeconds(120);          // 임시로 늘어난 스레드 유지 시간
        executor.setThreadNamePrefix("customWebClient-");   // 스레드 이름 prefix
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());    // 대기열이 꽉 찬 경우, 서블릿 스레드가 직접 처리

        executor.initialize();
        return executor;
    }
}
