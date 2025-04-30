package kr.ssok.userservice.repository;

import kr.ssok.userservice.config.TestAuditConfig;
import kr.ssok.userservice.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserRepository의 단위 테스트
 * JPA Repository 메서드들의 동작을 검증합니다.
 */
@DataJpaTest
@Import(TestAuditConfig.class) // JPA Auditing 활성화 설정 추가
@ActiveProfiles("test") // 테스트 프로파일 활성화
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * findById 메서드로 사용자 조회 테스트
     * 저장된 사용자를 ID로 조회하여 정확히 가져오는지 검증합니다.
     */
    @Test
    @DisplayName("findById 메서드로 사용자 조회 테스트")
    void findById_ReturnsUser() {
        // given
        User user = User.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode("encryptedPinCode")
                .build();
        
        User savedUser = entityManager.persistAndFlush(user);
        
        // when
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        
        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.get().getUsername()).isEqualTo("홍길동");
        assertThat(foundUser.get().getPhoneNumber()).isEqualTo("01012345678");
        assertThat(foundUser.get().getBirthDate()).isEqualTo("19900101");
        assertThat(foundUser.get().getPinCode()).isEqualTo("encryptedPinCode");
    }
    
    /**
     * existsByPhoneNumber 메서드로 전화번호 존재 여부 확인 테스트
     * 특정 전화번호를 가진 사용자의 존재 여부를 정확히 반환하는지 검증합니다.
     */
    @Test
    @DisplayName("existsByPhoneNumber 메서드로 전화번호 존재 여부 확인 테스트")
    void existsByPhoneNumber_ReturnsTrueForExistingNumber() {
        // given
        String existingPhoneNumber = "01012345678";
        String nonExistingPhoneNumber = "01098765432";
        
        User user = User.builder()
                .username("홍길동")
                .phoneNumber(existingPhoneNumber)
                .birthDate("19900101")
                .pinCode("encryptedPinCode")
                .build();
        
        entityManager.persistAndFlush(user);
        
        // when
        boolean existsResult = userRepository.existsByPhoneNumber(existingPhoneNumber);
        boolean notExistsResult = userRepository.existsByPhoneNumber(nonExistingPhoneNumber);
        
        // then
        assertThat(existsResult).isTrue();
        assertThat(notExistsResult).isFalse();
    }
    
    /**
     * save 메서드로 사용자 저장 테스트
     * 새 사용자를 저장하고 저장된 사용자의 속성과 생성된 ID를 검증합니다.
     */
    @Test
    @DisplayName("save 메서드로 사용자 저장 테스트")
    void save_PersistsUser() {
        // given
        User user = User.builder()
                .username("김철수")
                .phoneNumber("01087654321")
                .birthDate("19850505")
                .pinCode("anotherEncryptedPinCode")
                .build();
        
        // when
        User savedUser = userRepository.save(user);
        
        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull(); // ID가 생성되었는지 확인
        
        // 저장된 사용자를 다시 조회하여 검증
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("김철수");
        assertThat(foundUser.getPhoneNumber()).isEqualTo("01087654321");
        assertThat(foundUser.getBirthDate()).isEqualTo("19850505");
        assertThat(foundUser.getPinCode()).isEqualTo("anotherEncryptedPinCode");
    }
}
