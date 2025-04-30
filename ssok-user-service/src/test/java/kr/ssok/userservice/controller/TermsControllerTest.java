package kr.ssok.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.ssok.common.exception.CommonResponseStatus;
import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.service.TermsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TermsController 클래스에 대한 단위 테스트
 * 약관 목록 조회 및 약관 상세 조회 엔드포인트에 대한 테스트를 포함합니다.
 */
@WebMvcTest(TermsController.class)
@AutoConfigureMockMvc(addFilters = false) // Spring Security 필터 비활성화
@ExtendWith(MockitoExtension.class)
class TermsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TermsService termsService;

    /**
     * 약관 목록 조회 엔드포인트 테스트
     * 호출 시 HTTP 200 응답과 올바른 BaseResponse<List<TermsListResponseDto>>가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 목록 조회 엔드포인트 - 성공 테스트")
    void getTermsList_ReturnsCorrectResponse() throws Exception {
        // given
        List<TermsListResponseDto> termsList = Arrays.asList(
                new TermsListResponseDto(1L, "이용약관"),
                new TermsListResponseDto(2L, "개인정보처리방침"),
                new TermsListResponseDto(3L, "위치정보 이용약관")
        );

        when(termsService.getTermsList()).thenReturn(termsList);

        // when
        ResultActions result = mockMvc.perform(get("/api/terms"))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(CommonResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(CommonResponseStatus.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(3))
                .andExpect(jsonPath("$.result[0].termsId").value(1))
                .andExpect(jsonPath("$.result[0].title").value("이용약관"))
                .andExpect(jsonPath("$.result[1].termsId").value(2))
                .andExpect(jsonPath("$.result[1].title").value("개인정보처리방침"))
                .andExpect(jsonPath("$.result[2].termsId").value(3))
                .andExpect(jsonPath("$.result[2].title").value("위치정보 이용약관"));

        // verify
        verify(termsService).getTermsList();
    }

    /**
     * 약관 목록 조회 엔드포인트에서 서비스 메서드 호출 검증 테스트
     * 컨트롤러가 TermsService.getTermsList() 메서드를 정확히 호출하는지 검증합니다.
     */
    @Test
    @DisplayName("약관 목록 조회 엔드포인트 - 서비스 메서드 호출 검증 테스트")
    void getTermsList_CallsServiceMethod() throws Exception {
        // given
        List<TermsListResponseDto> emptyList = Arrays.asList();
        when(termsService.getTermsList()).thenReturn(emptyList);

        // when
        mockMvc.perform(get("/api/terms"))
                .andDo(print());

        // then
        verify(termsService, times(1)).getTermsList();
    }

    /**
     * 약관 상세 조회 엔드포인트 테스트
     * 유효한 약관 ID로 호출 시 HTTP 200 응답과 올바른 BaseResponse<TermsDetailResponseDto>가 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 상세 조회 엔드포인트 - 성공 테스트")
    void getTermsDetail_WithValidId_ReturnsCorrectResponse() throws Exception {
        // given
        Long termsId = 1L;
        TermsDetailResponseDto termsDetail = TermsDetailResponseDto.builder()
                .termsId(termsId)
                .title("이용약관")
                .content("이용약관 상세 내용입니다.")
                .build();

        when(termsService.getTermsDetail(termsId)).thenReturn(termsDetail);

        // when
        ResultActions result = mockMvc.perform(get("/api/terms/{termsId}", termsId))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(CommonResponseStatus.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value(CommonResponseStatus.SUCCESS.getMessage()))
                .andExpect(jsonPath("$.result.termsId").value(1))
                .andExpect(jsonPath("$.result.title").value("이용약관"))
                .andExpect(jsonPath("$.result.content").value("이용약관 상세 내용입니다."));

        // verify
        verify(termsService).getTermsDetail(termsId);
    }

    /**
     * 약관 상세 조회 엔드포인트에서 서비스 메서드 호출 시 전달된 매개변수 검증 테스트
     * 컨트롤러가 TermsService.getTermsDetail() 메서드를 호출할 때 올바른 약관 ID가 전달되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 상세 조회 엔드포인트 - 매개변수 검증 테스트")
    void getTermsDetail_PassesCorrectIdToService() throws Exception {
        // given
        Long termsId = 1L;
        TermsDetailResponseDto termsDetail = TermsDetailResponseDto.builder()
                .termsId(termsId)
                .title("이용약관")
                .content("이용약관 상세 내용입니다.")
                .build();

        when(termsService.getTermsDetail(anyLong())).thenReturn(termsDetail);

        // when
        mockMvc.perform(get("/api/terms/{termsId}", termsId))
                .andDo(print());

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(termsService).getTermsDetail(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(termsId);
    }

    /**
     * 존재하지 않는 약관 ID로 상세 조회 엔드포인트 호출 시 테스트
     * 유효하지 않은 약관 ID로 호출할 때 적절한 오류 응답이 반환되는지 검증합니다.
     */
    @Test
    @DisplayName("약관 상세 조회 엔드포인트 - 존재하지 않는 ID 테스트")
    void getTermsDetail_WithInvalidId_ReturnsErrorResponse() throws Exception {
        // given
        Long nonExistentTermsId = 999L;
        when(termsService.getTermsDetail(nonExistentTermsId))
                .thenThrow(new UserException(UserResponseStatus.TERMS_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(get("/api/terms/{termsId}", nonExistentTermsId))
                .andDo(print());

        // then
        result.andExpect(status().isBadRequest()) // UserResponseStatus.TERMS_NOT_FOUND의 HTTP 상태 코드에 따라 달라질 수 있음
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(UserResponseStatus.TERMS_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(UserResponseStatus.TERMS_NOT_FOUND.getMessage()));

        // verify
        verify(termsService).getTermsDetail(nonExistentTermsId);
    }
}
