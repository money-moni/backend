package kr.ssok.userservice.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 테스트 환경에서 사용할 Redis 구성
 * 외부 시스템 연동 테스트에서 실제 Redis를 사용하도록 설정합니다.
 */
@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    /**
     * 테스트용 Redis 연결 구성
     * 테스트 환경에서는 로컬 Redis 또는 테스트 전용 Redis 인스턴스 사용
     * 
     * @return Redis 연결 팩토리
     */
    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public RedisConnectionFactory redisConnectionFactory() {
        // 개발 환경에 맞게 호스트와 포트 설정
        // 기본값: localhost:6379
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("localhost");
        redisConfig.setPort(6379);
        
        // 필요한 경우 비밀번호 설정
        // redisConfig.setPassword("your-password");
        
        return new LettuceConnectionFactory(redisConfig);
    }

    /**
     * RedisTemplate 빈 구성
     * String 타입의 키와 값을 처리하는 RedisTemplate 설정
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return 구성된 RedisTemplate 인스턴스
     */
    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 일관된 직렬화를 위해 StringRedisSerializer 사용
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * StringRedisTemplate 빈 구성
     * 문자열 작업에 특화된 RedisTemplate
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return 구성된 StringRedisTemplate 인스턴스
     */
    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
