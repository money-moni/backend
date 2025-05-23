package kr.ssok.transferservice.config;

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
        // Kafka 프로듀서 설정을 담는 Map 객체 생성
        Map<String, Object> config = new HashMap<>();

        // Kafka 브로커 주소 지정
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 메시지의 key를 직렬화할 클래스 지정 (String → Byte 배열)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 메시지의 value를 직렬화할 클래스 지정 (String → Byte 배열)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // 위에서 정의한 설정으로 Kafka 프로듀서 팩토리 생성 후 반환
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
        // producerFactory()로부터 KafkaTemplate을 생성
        return new KafkaTemplate<>(producerFactory());
    }
}