package kr.ssok.transferservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 비동기(@Async) 작업 전용 스레드풀을 설정하는 클래스
 * - WebClient 논블로킹 호출 등을 별도 스레드풀에서 실행해
 *   서블릿 워커 스레드를 차단하지 않도록 구성
 */
@Configuration
@EnableAsync  // @Async 애노테이션 활성화
public class AsyncConfig {

    /**
     * 논리 코어당 기본 스레드풀 크기 배수.
     */
    @Value("${executor.corePoolSizeMultiplier}")
    private int corePoolMul;

    /**
     * 논리 코어당 최대 스레드풀 크기 배수.
     */
    @Value("${executor.maxPoolSizeMultiplier}")
    private int maxPoolMul;

    /**
     * WebClient 등의 비동기 I/O를 처리할 Executor를 빈으로 등록
     *
     * @return 설정된 ThreadPoolTaskExecutor 인스턴스
     */
    @Bean(name = "customExecutorWebClient")
    public Executor webClientExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        int cores = Runtime.getRuntime().availableProcessors(); // 물리·논리 코어 수
        exec.setCorePoolSize(cores * corePoolMul);              // 최소 스레드 수
        exec.setMaxPoolSize(cores * maxPoolMul);                // 최대 스레드 수
        exec.setQueueCapacity(500);                             // 대기 큐 용량
        exec.setKeepAliveSeconds(120);                          // 여분 스레드 유지 시간(초)
        exec.setThreadNamePrefix("customWebClient-");           // 스레드 이름 접두어

        // 큐가 포화될 때는 호출하는 스레드(Servlet 워커)가 직접 실행하여 요청 누락을 방지
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        exec.initialize();  // 초기화
        return exec;
    }
}