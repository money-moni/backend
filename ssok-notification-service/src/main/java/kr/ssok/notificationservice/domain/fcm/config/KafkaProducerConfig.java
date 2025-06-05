package kr.ssok.notificationservice.domain.fcm.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 메시지를 전송(produce)하기 위한 Kafka 프로듀서 설정 클래스
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka 프로듀서를 생성할 때 사용할 설정들을 정의한 Bean
     *
     * @return ProducerFactory<String, String> - 문자열 key/value를 전송하는 Kafka 프로듀서 팩토리
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka 메시지 전송을 위한 KafkaTemplate Bean 생성
     * - Spring에서 Kafka로 메시지를 보낼 때 주입받아 사용
     *
     * @return KafkaTemplate<String, String> - 문자열 기반 Kafka 메시지 전송용 템플릿
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}