package kr.ssok.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.userservice.config.TestSecurityConfig;
import kr.ssok.userservice.controller.UserController;
import kr.ssok.userservice.dto.response.UserInfoResponseDto;
import kr.ssok.userservice.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 사용자 정보 조회 테스트
 * 인증된 사용자의 정보를 조회하는 API를 테스트합니다.
 */
@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@ImportAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@WithMockUser
public class UserInfoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // 테스트 데이터
    private final Long TEST_USER_ID = 1L;
    private final String TEST_USERNAME = "홍길동";
    private final String TEST_PHONE_NUMBER = "01012345678";
    private final String TEST_PROFILE_IMAGE_URL = "https://example.com/profile.jpg";

    /**
     * 사용자 정보 조회 성공 테스트
     * 헤더에 사용자 ID를 포함하여 정보 조회 요청을 보내고 응답을 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 - 성공 테스트")
    void getUserInfo_Success() throws Exception {
        // Mock 서비스 동작 설정
        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .username(TEST_USERNAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .profileImage(TEST_PROFILE_IMAGE_URL)
                .build();
        
        when(userService.getUserInfo(TEST_USER_ID)).thenReturn(responseDto);

        // 사용자 정보 조회 요청
        ResultActions result = mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .header("X-User-Id", String.valueOf(TEST_USER_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)) // SUCCESS 코드
                .andExpect(jsonPath("$.result.username").value(TEST_USERNAME))
                .andExpect(jsonPath("$.result.phoneNumber").value(TEST_PHONE_NUMBER))
                .andExpect(jsonPath("$.result.profileImage").value(TEST_PROFILE_IMAGE_URL));

        // 서비스 호출 검증
        verify(userService).getUserInfo(TEST_USER_ID);
    }

    /**
     * 상세 응답 내용 검증 테스트
     * 응답으로 반환된 JSON을 객체로 파싱하여 사용자 정보를 세부적으로 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 - 응답 내용 상세 검증 테스트")
    void getUserInfo_ResponseContentValidation() throws Exception {
        // Mock 서비스 동작 설정 - 기본 정보 외에 추가 정보를 포함하도록 구성
        UserInfoResponseDto responseDto = UserInfoResponseDto.builder()
                .username(TEST_USERNAME)
                .phoneNumber(TEST_PHONE_NUMBER)
                .profileImage(TEST_PROFILE_IMAGE_URL)
                .build();
        
        when(userService.getUserInfo(TEST_USER_ID)).thenReturn(responseDto);

        // 사용자 정보 조회 요청 및 응답 받기
        MvcResult mvcResult = mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .header("X-User-Id", String.valueOf(TEST_USER_ID))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 내용을 문자열로 추출
        String responseContent = mvcResult.getResponse().getContentAsString();
        
        // 응답 JSON 파싱하여 객체로 변환
        UserInfoResponseDto extractedDto = objectMapper.readTree(responseContent)
                .path("result")
                .traverse(objectMapper)
                .readValueAs(UserInfoResponseDto.class);

        // 응답 객체 검증
        assertThat(extractedDto).isNotNull();
        assertThat(extractedDto.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(extractedDto.getPhoneNumber()).isEqualTo(TEST_PHONE_NUMBER);
        assertThat(extractedDto.getProfileImage()).isEqualTo(TEST_PROFILE_IMAGE_URL);
    }

    /**
     * 사용자 ID 헤더 누락 테스트
     * X-User-Id 헤더가 없을 때 오류가 발생하는지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 - 사용자 ID 헤더 누락 테스트")
    void getUserInfo_MissingUserIdHeader() throws Exception {
        // X-User-Id 헤더 없이 요청
        mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()); // 일반적으로 Bad Request 응답 기대

        // 서비스 호출 되지 않음 검증
        verify(userService, never()).getUserInfo(anyLong());
    }

    /**
     * 사용자 정보 조회 실패 - 존재하지 않는 사용자 테스트
     * 존재하지 않는 사용자 ID로 요청 시 적절한 오류가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("사용자 정보 조회 - 존재하지 않는 사용자 테스트")
    void getUserInfo_UserNotFound() throws Exception {
        // Mock 서비스 동작 설정 - 존재하지 않는 사용자 예외 발생
        when(userService.getUserInfo(999L)).thenThrow(
                new kr.ssok.userservice.exception.UserException(kr.ssok.userservice.exception.UserResponseStatus.USER_NOT_FOUND));

        // 사용자 정보 조회 요청
        mockMvc.perform(get("/api/users/info")
                .with(csrf())
                .header("X-User-Id", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()) // USER_NOT_FOUND는 BadRequest로 응답
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(5000)) // USER_NOT_FOUND 코드
                .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));

        // 서비스 호출 검증
        verify(userService).getUserInfo(999L);
    }
}
