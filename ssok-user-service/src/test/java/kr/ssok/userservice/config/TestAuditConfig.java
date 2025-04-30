package kr.ssok.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 테스트 환경에서 JPA Auditing 기능을 활성화하기 위한 설정 클래스
 * createdAt, updatedAt 필드가 자동으로 설정되도록 함
 */
@Configuration
@EnableJpaAuditing
public class TestAuditConfig {
    // 추가 설정이 필요한 경우 여기에 Bean 등록
}
