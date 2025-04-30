package kr.ssok.userservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능 활성화를 위한 설정 클래스
 * 엔티티의 createdAt, updatedAt 필드가 자동으로 설정되도록 함
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // 추가 설정이 필요한 경우 여기에 Bean 등록
}
