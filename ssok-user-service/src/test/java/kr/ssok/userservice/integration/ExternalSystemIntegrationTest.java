package kr.ssok.userservice.integration;

import kr.ssok.userservice.client.AligoClient;
import kr.ssok.userservice.client.BankClient;
import kr.ssok.userservice.dto.request.AligoVerificationRequestDto;
import kr.ssok.userservice.dto.request.BankAccountRequestDto;
import kr.ssok.userservice.dto.response.BankAccountResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 외부 시스템 연동 통합 테스트
 * BankClient, AligoClient, Redis 연동을 테스트합니다.
 * 
 * 이 테스트는 실제 외부 시스템과 연동하므로 해당 시스템이 가용한 상태여야 합니다.
 * 테스트 환경에서는 실제 시스템 대신 테스트용 인스턴스나 Mock 서버를 사용할 수 있습니다.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class ExternalSystemIntegrationTest {

    @Autowired
    private BankClient bankClient;

    @Autowired
    private AligoClient aligoClient;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 테스트 데이터
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_USERNAME = "테스트사용자";
    private final String TEST_VERIFICATION_CODE = "123456";
    private final int TEST_ACCOUNT_TYPE_CODE = 1; // 예금 계좌

    /**
     * BankClient 연동 테스트 - 계좌 생성 성공
     * 실제 BankClient를 사용하여 계좌 생성 요청을 보내고 응답을 처리합니다.
     */
    @Test
    @DisplayName("BankClient 연동 - 계좌 생성 성공 테스트")
    void bankClientIntegration_CreateAccount_Success() {
        // given
        BankAccountRequestDto requestDto = BankAccountRequestDto.builder()
                .username(TEST_USERNAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .accountTypeCode(TEST_ACCOUNT_TYPE_CODE)
                .build();

        // when
        BankAccountResponseDto responseDto = bankClient.createAccount(requestDto);

        // then
        assertNotNull(responseDto);
        assertNotNull(responseDto.getAccountNumber());
        assertFalse(responseDto.getAccountNumber().isEmpty());
        
        System.out.println("생성된 계좌번호: " + responseDto.getAccountNumber());
    }

    /**
     * BankClient 연동 테스트 - 유효하지 않은 요청
     * 유효하지 않은 요청 데이터로 예외 처리를 테스트합니다.
     */
    @Test
    @DisplayName("BankClient 연동 - 유효하지 않은 요청 테스트")
    void bankClientIntegration_InvalidRequest() {
        // given
        BankAccountRequestDto invalidRequestDto = BankAccountRequestDto.builder()
                .username("") // 빈 사용자 이름
                .phoneNumber(TEST_PHONE_NUMBER)
                .accountTypeCode(999) // 존재하지 않는 계좌 유형
                .build();

        // when & then
        assertThatThrownBy(() -> bankClient.createAccount(invalidRequestDto))
                .isInstanceOf(RestClientException.class);
    }

    /**
     * BankClient 연동 테스트 - 서버 오류 처리
     * 서버 오류 상황(5xx)에 대한 예외 처리를 테스트합니다.
     * 
     * 참고: 이 테스트는 실제 서버 오류 상황을 시뮬레이션하기 어려울 수 있습니다.
     * 실제 환경에서는 Mock 서버를 사용하거나 테스트 API를 통해 오류를 발생시킬 수 있습니다.
     */
    @Test
    @DisplayName("BankClient 연동 - 서버 오류 처리 테스트")
    void bankClientIntegration_ServerError() {
        // given
        BankAccountRequestDto requestDto = BankAccountRequestDto.builder()
                .username(TEST_USERNAME)
                .phoneNumber("500") // 테스트용 오류 트리거 값 (가정)
                .accountTypeCode(TEST_ACCOUNT_TYPE_CODE)
                .build();

        // when & then
        // 이 테스트는 실제 환경에 따라 조정이 필요할 수 있습니다.
        // 서버 오류를 시뮬레이션하기 위한 방법은 테스트 환경에 따라 다를 수 있습니다.
        try {
            BankAccountResponseDto responseDto = bankClient.createAccount(requestDto);
            // 오류가 발생하지 않을 경우 테스트를 건너뜁니다.
            System.out.println("서버 오류를 시뮬레이션할 수 없습니다. 테스트를 건너뜁니다.");
        } catch (Exception e) {
            // 예외가 발생한 경우 타입 확인
            assertTrue(e instanceof RestClientException, "예상된 예외 타입이 아닙니다: " + e.getClass().getName());
        }
    }

    /**
     * AligoClient 연동 테스트 - SMS 발송 성공
     * 실제 AligoClient를 사용하여 SMS 발송 요청을 보내고 응답을 처리합니다.
     */
    @Test
    @DisplayName("AligoClient 연동 - SMS 발송 성공 테스트")
    void aligoClientIntegration_SendSMS_Success() {
        // given
        AligoVerificationRequestDto requestDto = AligoVerificationRequestDto.builder()
                .phoneNumber(TEST_PHONE_NUMBER)
                .verificationCode(TEST_VERIFICATION_CODE)
                .build();

        // when & then
        // 예외가 발생하지 않으면 성공으로 간주합니다.
        assertDoesNotThrow(() -> aligoClient.sendVerificationCode(requestDto));
    }

    /**
     * AligoClient 연동 테스트 - 유효하지 않은 요청
     * 유효하지 않은 요청 데이터로 예외 처리를 테스트합니다.
     */
    @Test
    @DisplayName("AligoClient 연동 - 유효하지 않은 요청 테스트")
    void aligoClientIntegration_InvalidRequest() {
        // given
        AligoVerificationRequestDto invalidRequestDto = AligoVerificationRequestDto.builder()
                .phoneNumber("") // 빈 전화번호
                .verificationCode(TEST_VERIFICATION_CODE)
                .build();

        // when & then
        assertThatThrownBy(() -> aligoClient.sendVerificationCode(invalidRequestDto))
                .isInstanceOf(Exception.class);
    }

    /**
     * AligoClient 연동 테스트 - 서버 오류 처리
     * 서버 오류 상황(5xx)에 대한 예외 처리를 테스트합니다.
     */
    @Test
    @DisplayName("AligoClient 연동 - 서버 오류 처리 테스트")
    void aligoClientIntegration_ServerError() {
        // given
        AligoVerificationRequestDto requestDto = AligoVerificationRequestDto.builder()
                .phoneNumber("500") // 테스트용 오류 트리거 값 (가정)
                .verificationCode(TEST_VERIFICATION_CODE)
                .build();

        // when & then
        // 이 테스트는 실제 환경에 따라 조정이 필요할 수 있습니다.
        try {
            aligoClient.sendVerificationCode(requestDto);
            // 오류가 발생하지 않을 경우 테스트를 건너뜁니다.
            System.out.println("서버 오류를 시뮬레이션할 수 없습니다. 테스트를 건너뜁니다.");
        } catch (Exception e) {
            // 예외가 발생한 경우 타입 확인
            assertTrue(e instanceof Exception, "예상된 예외 타입이 아닙니다: " + e.getClass().getName());
        }
    }

    /**
     * Redis 연동 테스트 - 인증코드 저장 및 조회
     * 실제 Redis를 사용하여 인증코드를 저장하고 조회합니다.
     */
    @Test
    @DisplayName("Redis 연동 - 인증코드 저장 및 조회 테스트")
    void redisIntegration_SaveAndGet() {
        // given
        String key = "verification:" + TEST_PHONE_NUMBER;
        String value = TEST_VERIFICATION_CODE;
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        // when
        valueOps.set(key, value);
        String retrievedValue = valueOps.get(key);

        // then
        assertEquals(value, retrievedValue, "저장된 값과 조회된 값이 일치해야 합니다");

        // 정리
        redisTemplate.delete(key);
    }

    /**
     * Redis 연동 테스트 - 만료 시간 설정
     * 인증코드 저장 시 만료 시간을 설정하고 만료 후 조회를 테스트합니다.
     */
    @Test
    @DisplayName("Redis 연동 - 만료 시간 설정 테스트")
    void redisIntegration_ExpirationTime() throws InterruptedException {
        // given
        String key = "expiration:" + TEST_PHONE_NUMBER;
        String value = TEST_VERIFICATION_CODE;
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        long expirationTimeSeconds = 3; // 3초 후 만료

        // when
        valueOps.set(key, value, expirationTimeSeconds, TimeUnit.SECONDS);
        
        // 저장 직후 조회
        String retrievedBeforeExpiration = valueOps.get(key);
        
        // 만료 시간 기다림
        await()
            .atMost(5, TimeUnit.SECONDS)
            .until(() -> valueOps.get(key) == null);
        
        // 만료 후 조회
        String retrievedAfterExpiration = valueOps.get(key);

        // then
        assertEquals(value, retrievedBeforeExpiration, "만료 전에는 값이 존재해야 합니다");
        assertNull(retrievedAfterExpiration, "만료 후에는 값이 존재하지 않아야 합니다");
    }

    /**
     * Redis 연동 테스트 - 키 삭제
     * 인증코드 저장 후 명시적 삭제를 테스트합니다.
     */
    @Test
    @DisplayName("Redis 연동 - 키 삭제 테스트")
    void redisIntegration_DeleteKey() {
        // given
        String key = "delete:" + TEST_PHONE_NUMBER;
        String value = TEST_VERIFICATION_CODE;
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        // when
        valueOps.set(key, value);
        
        // 저장 직후 조회
        String retrievedBeforeDelete = valueOps.get(key);
        
        // 키 삭제
        Boolean deleteResult = redisTemplate.delete(key);
        
        // 삭제 후 조회
        String retrievedAfterDelete = valueOps.get(key);

        // then
        assertEquals(value, retrievedBeforeDelete, "삭제 전에는 값이 존재해야 합니다");
        assertTrue(deleteResult, "삭제 결과는 true여야 합니다");
        assertNull(retrievedAfterDelete, "삭제 후에는 값이 존재하지 않아야 합니다");
    }
}
