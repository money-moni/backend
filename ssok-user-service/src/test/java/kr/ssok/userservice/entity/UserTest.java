package kr.ssok.userservice.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * User 엔티티의 단위 테스트
 * 기본적인 엔티티 생성, 필드 검증 및 메서드 동작을 테스트합니다.
 */
public class UserTest {
    
    /**
     * User 엔티티 생성 및 필드 검증 테스트
     * 빌더 패턴을 사용하여 User 엔티티를 생성하고 각 필드가 올바르게 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("User 엔티티 생성 및 필드 검증 테스트")
    void createUser_ValidateFields() {
        // given
        Long id = 1L;
        String username = "홍길동";
        String phoneNumber = "01012345678";
        String birthDate = "19900101";
        String pinCode = "encryptedPinCode";
        
        // when
        User user = User.builder()
                .id(id)
                .username(username)
                .phoneNumber(phoneNumber)
                .birthDate(birthDate)
                .pinCode(pinCode)
                .build();
        
        // then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(user.getBirthDate()).isEqualTo(birthDate);
        assertThat(user.getPinCode()).isEqualTo(pinCode);
    }
    
    /**
     * updatePinCode 메서드 동작 검증 테스트
     * 핀 코드 업데이트 메서드가 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("updatePinCode 메서드 동작 검증 테스트")
    void updatePinCode_ValidateMethod() {
        // given
        String initialPinCode = "initialEncryptedPinCode";
        String updatedPinCode = "updatedEncryptedPinCode";
        
        User user = User.builder()
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode(initialPinCode)
                .build();
        
        // when
        user.updatePinCode(updatedPinCode);
        
        // then
        assertThat(user.getPinCode()).isEqualTo(updatedPinCode);
        assertThat(user.getPinCode()).isNotEqualTo(initialPinCode);
    }
    
    /**
     * ProfileImage와의 연관관계 매핑 검증 테스트
     * User와 ProfileImage 간의 양방향 1:1 관계 매핑이 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("ProfileImage와의 연관관계 매핑 검증 테스트")
    void userProfileImageRelationship_ValidateMapping() {
        // given
        User user = User.builder()
                .id(1L)
                .username("홍길동")
                .phoneNumber("01012345678")
                .birthDate("19900101")
                .pinCode("encryptedPinCode")
                .build();
        
        ProfileImage profileImage = ProfileImage.builder()
                .id(1L)
                .user(user)
                .storedFilename("profile.jpg")
                .url("https://example.com/profiles/profile.jpg")
                .contentType("image/jpeg")
                .build();
        
        // when
        // ProfileImage에서 User를 설정했으므로, 양방향 매핑을 위해 User에도 ProfileImage 설정
        // 실제 애플리케이션에서는 관계 설정 메서드를 사용하거나 Cascade를 활용하겠지만,
        // 이 테스트에서는 리플렉션을 사용하여 private 필드에 접근합니다.
        java.lang.reflect.Field profileImageField;
        try {
            profileImageField = User.class.getDeclaredField("profileImage");
            profileImageField.setAccessible(true);
            profileImageField.set(user, profileImage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set profileImage field", e);
        }
        
        // then
        assertThat(user.getProfileImage()).isNotNull();
        assertThat(user.getProfileImage().getId()).isEqualTo(profileImage.getId());
        assertThat(user.getProfileImage().getUser()).isEqualTo(user);
        assertThat(user.getProfileImage().getStoredFilename()).isEqualTo("profile.jpg");
        assertThat(user.getProfileImage().getUrl()).isEqualTo("https://example.com/profiles/profile.jpg");
        assertThat(user.getProfileImage().getContentType()).isEqualTo("image/jpeg");
        
        // 양방향 관계 확인
        assertThat(profileImage.getUser()).isEqualTo(user);
    }
}
