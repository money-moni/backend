package kr.ssok.userservice.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.userservice.config.TestSecurityConfig;
import kr.ssok.userservice.controller.TermsController;
import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;
import kr.ssok.userservice.service.TermsService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 약관 조회 통합 테스트
 * 약관 목록 조회 및 특정 약관 상세 조회 API를 테스트합니다.
 */
@WebMvcTest(TermsController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@ImportAutoConfiguration(exclude = {RedisAutoConfiguration.class})
@WithMockUser
public class TermsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TermsService termsService;

    // 테스트 데이터
    private final Long TERMS_ID_1 = 1L;
    private final Long TERMS_ID_2 = 2L;
    private final String TERMS_TITLE_1 = "서비스 이용약관";
    private final String TERMS_TITLE_2 = "개인정보 처리방침";
    private final String TERMS_CONTENT_1 = "서비스 이용약관 내용입니다...";
    private final String TERMS_CONTENT_2 = "개인정보 처리방침 내용입니다...";

    /**
     * 약관 목록 조회 -> 특정 약관 상세 조회 흐름 테스트
     * 1. 약관 목록 조회
     * 2. 조회된 목록에서 특정 약관 ID 선택하여 상세 조회
     */
    @Test
    @DisplayName("약관 조회 통합 흐름 - 성공 테스트")
    void termsIntegrationFlow_Success() throws Exception {
        // Mock 서비스 동작 설정 - 약관 목록
        List<TermsListResponseDto> termsList = Arrays.asList(
                TermsListResponseDto.builder()
                        .termsId(TERMS_ID_1)
                        .title(TERMS_TITLE_1)
                        .build(),
                TermsListResponseDto.builder()
                        .termsId(TERMS_ID_2)
                        .title(TERMS_TITLE_2)
                        .build()
        );
        
        when(termsService.getTermsList()).thenReturn(termsList);
        
        // Mock 서비스 동작 설정 - 약관 상세
        TermsDetailResponseDto termsDetail = TermsDetailResponseDto.builder()
                .termsId(TERMS_ID_1)
                .title(TERMS_TITLE_1)
                .content(TERMS_CONTENT_1)
                .build();
        
        when(termsService.getTermsDetail(TERMS_ID_1)).thenReturn(termsDetail);

        // 1. 약관 목록 조회 테스트
        MvcResult listResult = getTermsList();
        
        // 2. 목록에서 약관 ID 추출
        JsonNode rootNode = objectMapper.readTree(listResult.getResponse().getContentAsString());
        JsonNode resultNode = rootNode.path("result");
        Long firstTermsId = resultNode.get(0).path("termsId").asLong();
        assertThat(firstTermsId).isEqualTo(TERMS_ID_1);
        
        // 3. 특정 약관 상세 조회 테스트
        getTermsDetail(firstTermsId);
        
        // 4. 서비스 메소드 호출 검증
        verify(termsService).getTermsList();
        verify(termsService).getTermsDetail(TERMS_ID_1);
    }

    /**
     * 약관 목록 조회 테스트
     * 약관 목록 조회 API를 호출하고 응답을 검증합니다.
     * 
     * @return MvcResult 향후 추가 검증을 위한 MVC 결과 객체
     */
    private MvcResult getTermsList() throws Exception {
        // 약관 목록 조회 요청
        ResultActions result = mockMvc.perform(get("/api/terms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)) // SUCCESS 코드
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result[0].termsId").value(TERMS_ID_1))
                .andExpect(jsonPath("$.result[0].title").value(TERMS_TITLE_1))
                .andExpect(jsonPath("$.result[1].termsId").value(TERMS_ID_2))
                .andExpect(jsonPath("$.result[1].title").value(TERMS_TITLE_2));
        
        return result.andReturn();
    }

    /**
     * 약관 상세 조회 테스트
     * 특정 약관 ID로 상세 조회 API를 호출하고 응답을 검증합니다.
     * 
     * @param termsId 조회할 약관 ID
     * @return MvcResult 향후 추가 검증을 위한 MVC 결과 객체
     */
    private MvcResult getTermsDetail(Long termsId) throws Exception {
        // 약관 상세 조회 요청
        ResultActions result = mockMvc.perform(get("/api/terms/" + termsId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)) // SUCCESS 코드
                .andExpect(jsonPath("$.result.termsId").value(termsId))
                .andExpect(jsonPath("$.result.title").value(TERMS_TITLE_1))
                .andExpect(jsonPath("$.result.content").value(TERMS_CONTENT_1));
        
        return result.andReturn();
    }

    /**
     * 약관 목록 조회 상세 검증 테스트
     * 약관 목록 응답을 객체로 변환하여 더 세밀하게 검증합니다.
     */
    @Test
    @DisplayName("약관 목록 조회 - 응답 내용 상세 검증 테스트")
    void getTermsList_ResponseContentValidation() throws Exception {
        // Mock 서비스 동작 설정
        List<TermsListResponseDto> termsList = Arrays.asList(
                TermsListResponseDto.builder()
                        .termsId(TERMS_ID_1)
                        .title(TERMS_TITLE_1)
                        .build(),
                TermsListResponseDto.builder()
                        .termsId(TERMS_ID_2)
                        .title(TERMS_TITLE_2)
                        .build()
        );
        
        when(termsService.getTermsList()).thenReturn(termsList);

        // 약관 목록 조회 요청 및 응답 받기
        MvcResult mvcResult = mockMvc.perform(get("/api/terms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // 응답 내용을 문자열로 추출
        String responseContent = mvcResult.getResponse().getContentAsString();
        
        // 응답 JSON 파싱
        JsonNode rootNode = objectMapper.readTree(responseContent);
        JsonNode resultNode = rootNode.path("result");
        
        // 목록 크기 확인
        assertThat(resultNode.isArray()).isTrue();
        assertThat(resultNode.size()).isEqualTo(2);
        
        // 첫 번째 약관 정보 확인
        JsonNode firstTerms = resultNode.get(0);
        assertThat(firstTerms.path("termsId").asLong()).isEqualTo(TERMS_ID_1);
        assertThat(firstTerms.path("title").asText()).isEqualTo(TERMS_TITLE_1);
        
        // 두 번째 약관 정보 확인
        JsonNode secondTerms = resultNode.get(1);
        assertThat(secondTerms.path("termsId").asLong()).isEqualTo(TERMS_ID_2);
        assertThat(secondTerms.path("title").asText()).isEqualTo(TERMS_TITLE_2);
    }

    /**
     * 존재하지 않는 약관 조회 테스트
     * 존재하지 않는 약관 ID로 요청 시 적절한 오류가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 상세 조회 - 존재하지 않는 약관 테스트")
    void getTermsDetail_TermsNotFound() throws Exception {
        // Mock 서비스 동작 설정 - 존재하지 않는 약관 예외 발생
        when(termsService.getTermsDetail(999L)).thenThrow(
                new kr.ssok.userservice.exception.UserException(kr.ssok.userservice.exception.UserResponseStatus.TERMS_NOT_FOUND));

        // 약관 상세 조회 요청
        mockMvc.perform(get("/api/terms/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest()) // TERMS_NOT_FOUND는 BadRequest로 응답
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(5001)) // TERMS_NOT_FOUND 코드
                .andExpect(jsonPath("$.message").value("약관 정보를 찾을 수 없습니다."));

        // 서비스 호출 검증
        verify(termsService).getTermsDetail(999L);
    }

    /**
     * 약관 목록이 비어있는 경우 테스트
     * 약관이 없을 때 빈 배열이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 목록 조회 - 약관이 없는 경우 테스트")
    void getTermsList_EmptyList() throws Exception {
        // Mock 서비스 동작 설정 - 빈 목록 반환
        when(termsService.getTermsList()).thenReturn(List.of());

        // 약관 목록 조회 요청
        mockMvc.perform(get("/api/terms")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(2000)) // SUCCESS 코드
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result").isEmpty());

        // 서비스 호출 검증
        verify(termsService).getTermsList();
    }
}
