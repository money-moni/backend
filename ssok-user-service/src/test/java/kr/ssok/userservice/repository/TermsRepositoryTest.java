package kr.ssok.userservice.repository;

import kr.ssok.userservice.config.TestAuditConfig;
import kr.ssok.userservice.entity.Terms;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TermsRepository의 단위 테스트
 * JPA Repository 메서드들의 동작을 검증합니다.
 */
@DataJpaTest
@Import(TestAuditConfig.class) // JPA Auditing 활성화 설정 추가
public class TermsRepositoryTest {

    @Autowired
    private TermsRepository termsRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * findAll 메서드로 모든 약관 조회 테스트
     * 저장된 모든 약관을 정확히 조회하는지 검증합니다.
     */
    @Test
    @DisplayName("findAll 메서드로 모든 약관 조회 테스트")
    void findAll_ReturnsAllTerms() {
        // given
        Terms terms1 = Terms.builder()
                .title("이용약관")
                .content("이용약관 내용입니다.")
                .build();
        
        Terms terms2 = Terms.builder()
                .title("개인정보처리방침")
                .content("개인정보처리방침 내용입니다.")
                .build();
        
        Terms terms3 = Terms.builder()
                .title("위치정보 이용약관")
                .content("위치정보 이용약관 내용입니다.")
                .build();
        
        entityManager.persist(terms1);
        entityManager.persist(terms2);
        entityManager.persist(terms3);
        entityManager.flush();
        
        // when
        List<Terms> termsList = termsRepository.findAll();
        
        // then
        assertThat(termsList).isNotNull();
        assertThat(termsList).hasSize(3);
        
        // 약관 목록에 저장한 약관들이 모두 포함되어 있는지 확인
        assertThat(termsList).extracting(Terms::getTitle)
                .containsExactlyInAnyOrder("이용약관", "개인정보처리방침", "위치정보 이용약관");
    }
    
    /**
     * findAll 메서드로 약관이 없을 때 빈 목록 반환 테스트
     * 약관이 없을 때 빈 목록을 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("findAll 메서드로 약관이 없을 때 빈 목록 반환 테스트")
    void findAll_ReturnsEmptyList_WhenNoTermsExist() {
        // when
        List<Terms> termsList = termsRepository.findAll();
        
        // then
        assertThat(termsList).isNotNull();
        assertThat(termsList).isEmpty();
    }
    
    /**
     * findById 메서드로 특정 약관 조회 테스트
     * 특정 ID로 약관을 정확히 조회하는지 검증합니다.
     */
    @Test
    @DisplayName("findById 메서드로 특정 약관 조회 테스트")
    void findById_ReturnsTerm() {
        // given
        Terms terms = Terms.builder()
                .title("이용약관")
                .content("이용약관 상세 내용입니다.")
                .build();
        
        Terms savedTerms = entityManager.persistAndFlush(terms);
        
        // when
        Optional<Terms> foundTerms = termsRepository.findById(savedTerms.getId());
        
        // then
        assertThat(foundTerms).isPresent();
        assertThat(foundTerms.get().getId()).isEqualTo(savedTerms.getId());
        assertThat(foundTerms.get().getTitle()).isEqualTo("이용약관");
        assertThat(foundTerms.get().getContent()).isEqualTo("이용약관 상세 내용입니다.");
    }
    
    /**
     * findById 메서드로 존재하지 않는 약관 조회 테스트
     * 존재하지 않는 ID로 조회 시 빈 Optional을 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("findById 메서드로 존재하지 않는 약관 조회 테스트")
    void findById_ReturnsEmpty_WhenTermNotExists() {
        // given
        Long nonExistentId = 999L;
        
        // when
        Optional<Terms> foundTerms = termsRepository.findById(nonExistentId);
        
        // then
        assertThat(foundTerms).isEmpty();
    }
}
