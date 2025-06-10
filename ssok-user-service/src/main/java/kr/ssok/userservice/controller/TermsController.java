package kr.ssok.userservice.controller;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.common.exception.CommonResponseStatus;
import kr.ssok.common.logging.annotation.ControllerLogging;
import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;
import kr.ssok.userservice.service.TermsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/terms")
@RequiredArgsConstructor
@Slf4j
public class TermsController {
    private final TermsService termsService;

    /**
     * 약관 제목 목록 조회 API
     * 프론트에서 약관 제목 목록을 표시하기 위한 API
     * @return 약관 ID와 제목 목록
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<TermsListResponseDto>>> getTermsList() {
        List<TermsListResponseDto> responseDtoList = termsService.getTermsList();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(CommonResponseStatus.SUCCESS, responseDtoList));
    }

    /**
     * 약관 상세 조회 API
     * 프론트에서 특정 약관 제목을 클릭했을 때 내용을 표시하기 위한 API
     * @param termsId 조회할 약관 ID
     * @return 약관 ID, 제목, 내용을 포함한 상세 정보
     */
    @GetMapping("/{termsId}")
    public ResponseEntity<BaseResponse<TermsDetailResponseDto>> getTermsDetail(@PathVariable Long termsId) {
        TermsDetailResponseDto responseDto = termsService.getTermsDetail(termsId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new BaseResponse<>(CommonResponseStatus.SUCCESS, responseDto));
    }

}