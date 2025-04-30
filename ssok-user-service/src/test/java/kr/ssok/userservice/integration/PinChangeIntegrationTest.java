package kr.ssok.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.userservice.config.TestSecurityConfig;
import kr.ssok.userservice.controller.UserController;
import kr.ssok.userservice.dto.request.PhoneVerificationRequestDto;
import kr.ssok.userservice.dto.request.PinCodeRequestDto;
import kr.ssok.userservice.entity.User;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PIN 변경 통합 테스트
 * PIN 변경을 위한 인증 요청 -> 인증코드 확인 -> PIN 변경 요청의 전체 흐름을 테스트합니다.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@ImportAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@WithMockUser
public class PinChangeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    private final Long TEST_USER_ID = 1L;
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_VERIFICATION_CODE = "123456";
    private final String TEST_OLD_PIN = "111111";
    private final String TEST_NEW_PIN = "222222";
    private final String TEST_ENCODED_PIN = "encodedPin222222";

    @BeforeEach
    void setUp() {
        // Redis 모킹 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 테스트 유저 설정
        User testUser = User.builder()
                .id(TEST_USER_ID)
                .phoneNumber(TEST_PHONE_NUMBER)
                .pinCode(TEST_OLD_PIN)
                .build();

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(TEST_NEW_PIN)).thenReturn(TEST_ENCODED_PIN);
    }

    /**
     * PIN 변경 전체 프로세스 테스트
     * 1. PIN 변경을 위한 인증 요청
     * 2. 인증코드 확인
     * 3. PIN 변경 요청
     * 4. 변경된 PIN 코드 암호화 저장 확인
     */
    @Test
    @DisplayName("PIN 변경 통합 프로세스 - 성공 테스트")
    void pinChangeIntegrationSuccess() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).requestPinVerification(TEST_USER_ID);
        when(userService.verifyCodeForPinChange(TEST_PHONE_NUMBER, TEST_VERIFICATION_CODE, TEST_USER_ID)).thenReturn(true);
        
        // PIN 코드 암호화 확인을 위한 캡처
        ArgumentCaptor<String> pinCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(userService).updatePinCode(eq(TEST_USER_ID), pinCaptor.capture());
        
        // 1. PIN 변경을 위한 인증 요청 테스트
        requestPinVerification();

        // 2. 인증코드 확인 테스트
        verifyCodeForPinChange();

        // 3. PIN 변경 요청 테스트
        updatePinCode();
        
        // 4. 서비스 메소드 호출 검증
        verify(userService).requestPinVerification(TEST_USER_ID);
        verify(userService).verifyCodeForPinChange(TEST_PHONE_NUMBER, TEST_VERIFICATION_CODE, TEST_USER_ID);
        verify(userService).updatePinCode(eq(TEST_USER_ID), anyString());
        
        // 전달된 PIN 코드 확인
        String capturedPin = pinCaptor.getValue();
        assertThat(capturedPin).isEqualTo(TEST_NEW_PIN);
    }

    /**
     * PIN 변경을 위한 인증 요청 테스트
     * 사용자 ID로 PIN 변경 인증 요청을 보내고 성공 응답을 확인합니다.
     */
    private void requestPinVerification() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/api/users/pin/" + TEST_USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)); // SUCCESS 코드
    }

    /**
     * PIN 변경을 위한 인증코드 확인 테스트
     * 발송된 인증코드의 유효성을 확인합니다.
     */
    private void verifyCodeForPinChange() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber(TEST_PHONE_NUMBER);
        requestDto.setVerificationCode(TEST_VERIFICATION_CODE);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", TEST_USER_ID)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)); // SUCCESS 코드
    }

    /**
     * PIN 코드 변경 요청 테스트
     * 새로운 PIN 코드로 변경 요청을 보내고 성공 응답을 확인합니다.
     */
    private void updatePinCode() throws Exception {
        // given
        PinCodeRequestDto requestDto = new PinCodeRequestDto();
        requestDto.setUserId(TEST_USER_ID);
        requestDto.setPinCode(TEST_NEW_PIN);

        // when
        ResultActions result = mockMvc.perform(patch("/api/users/pin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)); // SUCCESS 코드
    }

    /**
     * PIN 변경 프로세스 실패 시나리오 - 인증코드 불일치
     * 인증코드가 일치하지 않을 때 PIN 변경 프로세스가 실패하는지 테스트합니다.
     */
    @Test
    @DisplayName("PIN 변경 통합 프로세스 - 인증코드 불일치 테스트")
    void pinChangeIntegrationFailure_InvalidVerificationCode() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).requestPinVerification(TEST_USER_ID);
        when(userService.verifyCodeForPinChange(TEST_PHONE_NUMBER, "999999", TEST_USER_ID)).thenReturn(false);

        // 1. PIN 변경을 위한 인증 요청
        mockMvc.perform(post("/api/users/pin/" + TEST_USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 2. 잘못된 인증코드로 확인 요청
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber(TEST_PHONE_NUMBER);
        requestDto.setVerificationCode("999999"); // 잘못된 인증코드

        mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", TEST_USER_ID)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(4001)); // CODE_VERIFICATION_FAIL 코드

        // 서비스 메소드 호출 검증
        verify(userService).requestPinVerification(TEST_USER_ID);
        verify(userService).verifyCodeForPinChange(TEST_PHONE_NUMBER, "999999", TEST_USER_ID);
        verify(userService, never()).updatePinCode(anyLong(), anyString()); // PIN 변경 메소드는 호출되지 않아야 함
    }

    /**
     * PIN 변경 프로세스 실패 시나리오 - PIN 변경 권한 없음
     * 인증 없이 PIN 변경을 시도할 때 실패하는지 테스트합니다.
     */
    @Test
    @DisplayName("PIN 변경 통합 프로세스 - PIN 변경 권한 없음 테스트")
    void pinChangeIntegrationFailure_NoAuthForPinChange() throws Exception {
        // Mock 서비스 동작 설정 - PIN 변경 요청 시 권한 없음 예외 발생
        doThrow(new kr.ssok.userservice.exception.UserException(kr.ssok.userservice.exception.UserResponseStatus.PIN_CHANGE_AUTH_REQUIRED))
                .when(userService).updatePinCode(eq(TEST_USER_ID), anyString());

        // PIN 변경 요청 (인증 과정 생략)
        PinCodeRequestDto requestDto = new PinCodeRequestDto();
        requestDto.setUserId(TEST_USER_ID);
        requestDto.setPinCode(TEST_NEW_PIN);

        mockMvc.perform(patch("/api/users/pin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized()) // PIN_CHANGE_AUTH_REQUIRED 상태 코드 401
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(4017)); // PIN_CHANGE_AUTH_REQUIRED 코드

        // 서비스 메소드 호출 검증
        verify(userService).updatePinCode(TEST_USER_ID, TEST_NEW_PIN);
        verify(userService, never()).requestPinVerification(anyLong()); // 인증 요청은 발생하지 않음
        verify(userService, never()).verifyCodeForPinChange(anyString(), anyString(), anyLong()); // 인증코드 확인도 발생하지 않음
    }

    /**
     * PIN 변경 프로세스 실패 시나리오 - 사용자 정보 불일치
     * 요청된 전화번호가 사용자 정보와 일치하지 않을 때 실패하는지 테스트합니다.
     */
    @Test
    @DisplayName("PIN 변경 통합 프로세스 - 사용자 정보 불일치 테스트")
    void pinChangeIntegrationFailure_PhoneNumberMismatch() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).requestPinVerification(TEST_USER_ID);
        
        // 전화번호 불일치 예외 발생
        when(userService.verifyCodeForPinChange("01099998888", TEST_VERIFICATION_CODE, TEST_USER_ID))
                .thenThrow(new kr.ssok.userservice.exception.UserException(kr.ssok.userservice.exception.UserResponseStatus.PHONE_NUMBER_MISMATCH));

        // 1. PIN 변경을 위한 인증 요청
        mockMvc.perform(post("/api/users/pin/" + TEST_USER_ID)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 2. 잘못된 전화번호로 인증코드 확인 요청
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber("01099998888"); // 다른 전화번호
        requestDto.setVerificationCode(TEST_VERIFICATION_CODE);

        mockMvc.perform(post("/api/users/pin/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-User-Id", TEST_USER_ID)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(4018)); // PHONE_NUMBER_MISMATCH 코드

        // 서비스 메소드 호출 검증
        verify(userService).requestPinVerification(TEST_USER_ID);
        verify(userService).verifyCodeForPinChange("01099998888", TEST_VERIFICATION_CODE, TEST_USER_ID);
        verify(userService, never()).updatePinCode(anyLong(), anyString()); // PIN 변경 메소드는 호출되지 않아야 함
    }
}
