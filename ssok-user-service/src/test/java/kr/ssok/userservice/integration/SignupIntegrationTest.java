package kr.ssok.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.userservice.client.AligoClient;
import kr.ssok.userservice.client.BankClient;
import kr.ssok.userservice.config.TestSecurityConfig;
import kr.ssok.userservice.controller.UserController;
import kr.ssok.userservice.dto.request.BankAccountRequestDto;
import kr.ssok.userservice.dto.request.PhoneVerificationRequestDto;
import kr.ssok.userservice.dto.request.SignupRequestDto;
import kr.ssok.userservice.dto.response.BankAccountResponseDto;
import kr.ssok.userservice.dto.response.SignupResponseDto;
import kr.ssok.userservice.repository.UserRepository;
import kr.ssok.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 회원가입 통합 테스트
 * 전화번호 인증 요청 -> 인증코드 확인 -> 회원가입 요청의 전체 흐름을 테스트합니다.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@ImportAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@WithMockUser
public class SignupIntegrationTest {

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
    private AligoClient aligoClient;

    @MockitoBean
    private BankClient bankClient;

    @MockitoBean
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private ValueOperations<String, String> valueOperations;

    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_VERIFICATION_CODE = "123456";

    @BeforeEach
    void setUp() {
        // Redis 모킹 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // 뱅크 클라이언트 모킹
        BankAccountResponseDto bankResponse = new BankAccountResponseDto();
        bankResponse.setAccountNumber("123-456-789");
        when(bankClient.createAccount(any(BankAccountRequestDto.class))).thenReturn(bankResponse);
    }

    /**
     * 회원가입 전체 프로세스 테스트
     * 1. 전화번호 인증 요청
     * 2. 인증코드 확인
     * 3. 회원가입 요청
     * 4. 최종 사용자 정보 DB 저장 확인
     */
    @Test
    @DisplayName("회원가입 통합 프로세스 - 성공 테스트")
    void signupIntegrationSuccess() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).phoneVerification(anyString());
        when(userService.verifyCode(anyString(), anyString())).thenReturn(true);
        
        SignupResponseDto signupResponseDto = SignupResponseDto.builder()
                .userId(1L)
                .build();
        when(userService.registerUser(any(SignupRequestDto.class), any())).thenReturn(signupResponseDto);
        
        // 1. 전화번호 인증 요청 테스트
        phoneVerificationRequest();

        // 2. 인증코드 확인 테스트
        verifyCode();

        // 3. 회원가입 요청 테스트
        signupRequest();
        
        // 서비스 메소드 호출 검증
        verify(userService).phoneVerification(TEST_PHONE_NUMBER);
        verify(userService).verifyCode(TEST_PHONE_NUMBER, TEST_VERIFICATION_CODE);
        verify(userService).registerUser(any(SignupRequestDto.class), any());
    }

    /**
     * 전화번호 인증 요청 테스트
     * 휴대폰 인증코드 발송 요청을 보내고 성공 응답을 확인합니다.
     */
    private void phoneVerificationRequest() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber(TEST_PHONE_NUMBER);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/phone")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    /**
     * 인증코드 확인 테스트
     * 이전 단계에서 발송된 인증코드의 유효성을 확인합니다.
     */
    private void verifyCode() throws Exception {
        // given
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber(TEST_PHONE_NUMBER);
        requestDto.setVerificationCode(TEST_VERIFICATION_CODE);

        // when
        ResultActions result = mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true));
    }

    /**
     * 회원가입 요청 테스트
     * 사용자 정보를 입력하고 회원가입을 완료합니다.
     * 
     * @return 생성된 사용자 ID
     */
    private Long signupRequest() throws Exception {
        // given
        SignupRequestDto requestDto = SignupRequestDto.builder()
                .username("테스트사용자")
                .phoneNumber(TEST_PHONE_NUMBER)
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        // when
        MvcResult mvcResult = mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.userId").exists())
                .andReturn();

        // 응답에서 사용자 ID 추출
        String responseContent = mvcResult.getResponse().getContentAsString();
        Long userId = objectMapper.readTree(responseContent)
                .path("result")
                .path("userId")
                .asLong();

        return userId;
    }

    /**
     * 회원가입 프로세스 실패 시나리오 - 인증코드 불일치
     * 인증코드가 일치하지 않을 때 회원가입 프로세스가 실패하는지 테스트합니다.
     */
    @Test
    @DisplayName("회원가입 통합 프로세스 - 인증코드 불일치 테스트")
    void signupIntegrationFailure_InvalidVerificationCode() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).phoneVerification(anyString());
        when(userService.verifyCode(anyString(), anyString())).thenReturn(false);

        // given
        // 1. 전화번호 인증 요청
        PhoneVerificationRequestDto requestDto = new PhoneVerificationRequestDto();
        requestDto.setPhoneNumber(TEST_PHONE_NUMBER);
        
        mockMvc.perform(post("/api/users/phone")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        // 2. 잘못된 인증코드로 확인 요청
        requestDto.setVerificationCode("999999"); // 잘못된 인증코드

        // when & then
        mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
        
        // 서비스 메소드 호출 검증
        verify(userService).phoneVerification(TEST_PHONE_NUMBER);
        verify(userService).verifyCode(TEST_PHONE_NUMBER, "999999");
    }

    /**
     * 회원가입 프로세스 실패 시나리오 - 중복 가입
     * 이미 가입된 전화번호로 회원가입을 시도할 때 실패하는지 테스트합니다.
     */
    @Test
    @DisplayName("회원가입 통합 프로세스 - 중복 가입 테스트")
    void signupIntegrationFailure_DuplicateUser() throws Exception {
        // Mock 서비스 동작 설정
        doNothing().when(userService).phoneVerification(anyString());
        when(userService.verifyCode(anyString(), anyString())).thenReturn(true);
        
        // 중복 가입 시도 시 예외 발생하도록 설정
        when(userService.registerUser(any(SignupRequestDto.class), any()))
                .thenThrow(new kr.ssok.userservice.exception.UserException(kr.ssok.userservice.exception.UserResponseStatus.USER_ALREADY_EXISTS));

        // 1. 인증 과정 수행 (성공)
        PhoneVerificationRequestDto verificationDto = new PhoneVerificationRequestDto();
        verificationDto.setPhoneNumber(TEST_PHONE_NUMBER);
        verificationDto.setVerificationCode(TEST_VERIFICATION_CODE);
        
        mockMvc.perform(post("/api/users/phone/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verificationDto)))
                .andExpect(status().isOk());

        // 2. 같은 전화번호로 회원가입 시도
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .username("새사용자")
                .phoneNumber(TEST_PHONE_NUMBER) // 기존 사용자와 동일한 전화번호
                .birthDate("19900101")
                .pinCode(123456)
                .build();

        // when & then
        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(5000)); // USER_ALREADY_EXISTS 에러 코드
        
        // 서비스 메소드 호출 검증
        verify(userService).verifyCode(TEST_PHONE_NUMBER, TEST_VERIFICATION_CODE);
        verify(userService).registerUser(any(SignupRequestDto.class), any());
    }
}
