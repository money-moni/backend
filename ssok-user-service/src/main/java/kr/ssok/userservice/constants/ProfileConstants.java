package kr.ssok.userservice.constants;

/**
 * 프로필 이미지 관련 상수 클래스
 */
public class ProfileConstants {
    /**
     * S3에 미리 업로드된 기본 프로필 이미지 파일명
     */
    public static final String DEFAULT_IMAGE_FILENAME = "noImage.webp";
    
    /**
     * 기본 프로필 이미지의 Content-Type
     */
    public static final String DEFAULT_IMAGE_CONTENT_TYPE = "image/webp";
    
    private ProfileConstants() {
        // 유틸리티 클래스 - 인스턴스 생성 방지
    }
}
