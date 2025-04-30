package kr.ssok.userservice.entity;

import kr.ssok.common.entity.TimeStamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Terms 엔티티의 단위 테스트
 * 기본적인 엔티티 생성 및 필드 검증을 테스트합니다.
 */
public class TermsTest {
    
    /**
     * Terms 엔티티 생성 및 필드 검증 테스트
     * 빌더 패턴을 사용하여 Terms 엔티티를 생성하고 각 필드가 올바르게 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("Terms 엔티티 생성 및 필드 검증 테스트")
    void createTerms_ValidateFields() {
        // given
        Long id = 1L;
        String title = "이용약관";
        String content = "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>" +
                "<h1>이용약관</h1><p>이 약관은 ...</p><h1>이용약관</h1><p>이 약관은 ...</p>";

        
        // when
        Terms terms = Terms.builder()
                .id(id)
                .title(title)
                .content(content)
                .build();
        
        // then
        assertThat(terms).isNotNull();
        assertThat(terms.getId()).isEqualTo(id);
        assertThat(terms.getTitle()).isEqualTo(title);
        assertThat(terms.getContent()).isEqualTo(content);
    }
    
    /**
     * Terms 엔티티의 TimeStamp 상속 검증 테스트
     * Terms 엔티티가 TimeStamp를 상속받아 생성/수정 시간 필드를 갖는지 검증합니다.
     * 참고: 실제 JPA 환경이 아닌 단위 테스트에서는 AuditingEntityListener가 동작하지 않으므로
     * createdAt과 updatedAt은 null이 됩니다. 이 테스트는 해당 필드가 존재하는지만 검증합니다.
     */
    @Test
    @DisplayName("Terms 엔티티의 TimeStamp 상속 검증 테스트")
    void termsEntity_InheritsTimeStamp() {
        // given
        Terms terms = Terms.builder()
                .title("이용약관")
                .content("이용약관 내용")
                .build();
        
        // then
        // 단위 테스트에서는 AuditingEntityListener가 동작하지 않으므로 필드가 null인지만 확인
        // 실제 통합 테스트에서는 해당 필드가 자동으로 채워지는지 검증해야 함
        assertThat(terms).isInstanceOf(TimeStamp.class);
    }
}
