package kr.ssok.notificationservice.domain.fcm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    /**
     * Kafka RetryTopic 처리에 필요한 TaskScheduler Bean 등록
     *
     * @return TaskScheduler
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("retry-task-");
        scheduler.setPoolSize(2); // 필요 시 조정 가능
        return scheduler;
    }
}