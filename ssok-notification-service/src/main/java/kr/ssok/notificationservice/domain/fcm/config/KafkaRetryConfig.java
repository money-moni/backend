package kr.ssok.notificationservice.domain.fcm.config;

import kr.ssok.notificationservice.domain.fcm.handler.KafkaDltHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.*;
import org.springframework.kafka.support.EndpointHandlerMethod;

/**
 * Kafka DLQ 및 재시도 처리 전역 설정 클래스
 *
 * Spring Kafka의 RetryTopicConfigurationBuilder를 사용하여 다음 기능을 구성
 * - 최대 재시도 횟수
 * - 재시도 간격
 * - Retry Topic 및 DLT Topic 자동 생성
 * - DLT 실패 메시지 후처리 메서드 지정
 * - 전체 KafkaListener에 공통 적용
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaRetryConfig extends RetryTopicConfigurationSupport {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ConcurrentKafkaListenerContainerFactory<String, String> containerFactory;

    @Value("${kafka.retry.max-attempts}")
    private int maxAttempts;

    @Value("${kafka.retry.backoff-ms}")
    private long backoffMs;

    @Value("${kafka.retry.topic-replica}")
    private int replicaCount;

    @Value("${kafka.retry.replication-factor}")
    private short replicationFactor;

    /**
     * Kafka 메시지 재시도 및 DLQ 전송을 위한 전역 설정
     */
    @Bean
    public RetryTopicConfiguration retryTopicConfiguration() {
        return RetryTopicConfigurationBuilder
                .newInstance()
                .autoCreateTopicsWith(replicaCount, replicationFactor) // retry, dlt 토픽 자동 생성
                .maxAttempts(maxAttempts)                           // 재시도 횟수 설정
                .fixedBackOff(backoffMs)                            // 재시도 간격 설정
                .setTopicSuffixingStrategy(TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE)
                .listenerFactory(containerFactory)                  // 리스너 팩토리 설정
                .dltHandlerMethod(new EndpointHandlerMethod(KafkaDltHandler.class, "handleDltMessage")) // DLQ 후처리 핸들러 등록
                .create(kafkaTemplate);
    }
}
