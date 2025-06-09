package kr.ssok.accountservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 기능을 활성화하기 위한 설정 클래스
 *
 * <p>이 설정을 통해 {@code @CreatedDate}, {@code @LastModifiedDate} 등의 어노테이션이
 * JPA Entity에 적용될 수 있도록 JPA 감사(Auditing) 기능을 활성화합니다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
