package kr.ssok.notificationservice.domain.fcm.config;

import kr.ssok.notificationservice.global.exception.NotificationPermanentException;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 메시지를 수신(consume)하기 위한 Kafka 컨슈머 설정 클래스
 * - KafkaListener를 사용할 수 있도록 설정을 제공하며,
 *   메시지를 자동으로 JSON 문자열로 받아올 수 있게 구성
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.group-id}")
    private String groupId;

    /**
     * Kafka Consumer 설정 정보를 담은 ConsumerFactory Bean
     *
     * @return ConsumerFactory<String, String> - Kafka에서 String key/value 메시지를 수신하기 위한 팩토리
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka 브로커 주소
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 컨슈머 그룹 ID 설정: 같은 그룹 내 컨슈머는 서로 파티션을 분할 소비
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Kafka 오프셋 처리 방식 설정:
        // earliest → 가장 오래된 메시지부터 소비 (처음 구독 시 유용)
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Kafka 메시지의 key를 디코딩할 디시리얼라이저 클래스 (문자열로 변환)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Kafka 메시지의 value를 디코딩할 디시리얼라이저 클래스 (문자열로 변환)
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // 설정을 기반으로 ConsumerFactory 생성 및 반환
        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Main 토픽 전용 ConcurrentKafkaListenerContainerFactory Bean
     * - NotificationPermanentException 발생 시, 재시도 없이 즉시 DLT로 보낸다.
     *
     * @return ConcurrentKafkaListenerContainerFactory<String, String>
     */
    @Bean(name = "mainKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> mainKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Permanent 예외 발생 시 Retry 없이 바로 DLT로 보내기
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(0L, 0L)  // 재시도 없이 즉시 DLT
        );
        // NotificationPermanentException이 발생하면 즉시 DLT (Retry 대상 제외)
        errorHandler.addNotRetryableExceptions(NotificationPermanentException.class);
        factory.setCommonErrorHandler(errorHandler);

        // 수동 커밋 설정
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }

    /**
     * Recovery 토픽 전용 ConcurrentKafkaListenerContainerFactory Bean
     * - 실패 시 단 한 번만 시도하고, swallow 하도록 설정 (무한루프 방지)
     *
     * @return ConcurrentKafkaListenerContainerFactory<String, String>
     */
    @Bean(name = "recoveryKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, String> recoveryKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Recovery 단계에서는 재시도 없이 바로 swallow(예외 무시) 후 다음 메시지로 넘어감
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(0L, 0L)
        );
        factory.setCommonErrorHandler(errorHandler);

        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        return factory;
    }
}