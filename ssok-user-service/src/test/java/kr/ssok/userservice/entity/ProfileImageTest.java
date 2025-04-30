package kr.ssok.userservice.entity;

import kr.ssok.common.entity.TimeStamp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProfileImage 엔티티의 단위 테스트
 * 기본적인 엔티티 생성, 필드 검증 및 연관관계 매핑을 테스트합니다.
 */
public class ProfileImageTest {
    
    /**
     * ProfileImage 엔티티 생성 및 필드 검증 테스트
     * 빌더 패턴을 사용하여 ProfileImage 엔티티를 생성하고 각 필드가 올바르게 설정되는지 검증합니다.
     */
    @Test
    @DisplayName("ProfileImage 엔티티 생성 및 필드 검증 테스트")
    void createProfileImage_ValidateFields() {
        // given
        Long id = 1L;
        String storedFilename = "e7b8f3a2-9df4-456e.png";
        String url = "https://example.com/profiles/e7b8f3a2-9df4-456e.png";
        String contentType = "image/png";
        
        // User 객체도 필요하지만, 필드 검증 테스트에서는 null로 설정
        
        // when
        ProfileImage profileImage = ProfileImage.builder()
                .id(id)
                .storedFilename(storedFilename)
                .url(url)
                .contentType(contentType)
                .build();
        
        // then
        assertThat(profileImage).isNotNull();
        assertThat(profileImage.getId()).isEqualTo(id);
        assertThat(profileImage.getStoredFilename()).isEqualTo(storedFilename);
        assertThat(profileImage.getUrl()).isEqualTo(url);
        assertThat(profileImage.getContentType()).isEqualTo(contentType);
        assertThat(profileImage.getUser()).isNull(); // 설정하지 않았으므로 null
    }
    
    /**
     * User와의 연관관계 매핑 검증 테스트
     * ProfileImage와 User 간의 양방향 1:1 관계 매핑이 올바르게 동작하는지 검증합니다.
     */
    @Test
    @DisplayName("User와의 연관관계 매핑 검증 테스트")
    void profileImageUserRelationship_ValidateMapping() {
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
        // User에도 ProfileImage 설정 (양방향 매핑을 위해)
        java.lang.reflect.Field profileImageField;
        try {
            profileImageField = User.class.getDeclaredField("profileImage");
            profileImageField.setAccessible(true);
            profileImageField.set(user, profileImage);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set profileImage field", e);
        }
        
        // then
        // ProfileImage -> User 관계 검증
        assertThat(profileImage.getUser()).isNotNull();
        assertThat(profileImage.getUser().getId()).isEqualTo(user.getId());
        assertThat(profileImage.getUser().getUsername()).isEqualTo("홍길동");
        
        // User -> ProfileImage 관계 검증
        assertThat(user.getProfileImage()).isNotNull();
        assertThat(user.getProfileImage().getId()).isEqualTo(profileImage.getId());
        assertThat(user.getProfileImage().getStoredFilename()).isEqualTo("profile.jpg");
    }
    
    /**
     * ProfileImage 엔티티의 TimeStamp 상속 검증 테스트
     * ProfileImage 엔티티가 TimeStamp를 상속받아 생성/수정 시간 필드를 갖는지 검증합니다.
     */
    @Test
    @DisplayName("ProfileImage 엔티티의 TimeStamp 상속 검증 테스트")
    void profileImageEntity_InheritsTimeStamp() {
        // given
        ProfileImage profileImage = ProfileImage.builder()
                .storedFilename("profile.jpg")
                .url("https://example.com/profiles/profile.jpg")
                .contentType("image/jpeg")
                .build();
        
        // then
        // 단위 테스트에서는 AuditingEntityListener가 동작하지 않으므로 TimeStamp 상속 여부만 확인
        assertThat(profileImage).isInstanceOf(TimeStamp.class);
    }
}
