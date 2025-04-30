package kr.ssok.userservice.util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

/**
 * 테스트에 필요한 유틸리티와 헬퍼 메서드 모음
 */
@TestConfiguration
@Profile("test")
public class TestUtils {

    /**
     * 외부 API 호출용 RestTemplate 빈
     * 외부 시스템 연동 테스트에서 실제 API 호출에 사용됩니다.
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 테스트 중 사용할 수 있는 테스트 데이터 생성 메서드
     * 
     * @param prefix 접두사
     * @return 테스트 전화번호
     */
    public static String generateTestPhoneNumber(String prefix) {
        return prefix + System.currentTimeMillis() % 10000;
    }

    /**
     * 테스트 계정 이름 생성
     * 
     * @return 테스트 계정 이름
     */
    public static String generateTestUserName() {
        return "테스트사용자" + System.currentTimeMillis() % 1000;
    }
}
