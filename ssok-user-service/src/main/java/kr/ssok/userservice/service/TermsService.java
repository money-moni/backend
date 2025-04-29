package kr.ssok.userservice.service;

import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;

import java.util.List;

public interface TermsService {
    /**
     * 약관 목록 조회 (제목만 포함)
     * @return 약관 목록 (ID, 제목)
     */
    List<TermsListResponseDto> getTermsList();

    /**
     * 약관 상세 조회 (제목과 내용 포함)
     * @param termsId 조회할 약관 ID
     * @return 약관 상세 정보 (ID, 제목, 내용)
     */
    TermsDetailResponseDto getTermsDetail(Long termsId);
}
