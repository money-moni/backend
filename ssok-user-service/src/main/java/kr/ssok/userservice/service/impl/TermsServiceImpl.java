package kr.ssok.userservice.service.impl;

import kr.ssok.common.logging.annotation.ServiceLogging;
import kr.ssok.userservice.dto.response.TermsDetailResponseDto;
import kr.ssok.userservice.dto.response.TermsListResponseDto;
import kr.ssok.userservice.entity.Terms;
import kr.ssok.userservice.exception.UserException;
import kr.ssok.userservice.exception.UserResponseStatus;
import kr.ssok.userservice.repository.TermsRepository;
import kr.ssok.userservice.service.TermsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TermsServiceImpl implements TermsService {
    private final TermsRepository termsRepository;

    /**
     * 모든 약관 목록 조회 (제목만 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public List<TermsListResponseDto> getTermsList() {
        log.info("약관 목록 조회");
        List<Terms> termsList = termsRepository.findAll();

        return termsList.stream()
                .map(TermsListResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 약관 상세 조회 (제목 및 내용 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public TermsDetailResponseDto getTermsDetail(Long termsId) {
        log.info("약관 상세 조회: {}", termsId);
        Terms terms = termsRepository.findById(termsId)
                .orElseThrow(() -> new UserException(UserResponseStatus.TERMS_NOT_FOUND));

        return TermsDetailResponseDto.builder()
                .termsId(termsId)
                .title(terms.getTitle())
                .content(terms.getContent())
                .build();
    }
}
