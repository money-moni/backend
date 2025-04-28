package kr.ssok.accountservice.service;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;

/**
 * 계좌 관련 비즈니스 로직을 정의하는 Service 인터페이스
 *
 * <p>주요 기능:
 * <ul>
 *     <li>연동 계좌 생성</li>
 *     <li>연동 계좌 전체 조회</li>
 *     <li>연동 계좌 상세 조회</li>
 *     <li>연동 계좌 삭제</li>
 *     <li>계좌 별칭 변경</li>
 *     <li>주 계좌 변경</li>
 * </ul>
 * </p>
 */
public interface AccountService {
    /**
     * 사용자의 요청에 따라 연동 계좌를 생성합니다.
     *
     * @param userId 사용자 ID
     * @param createAccountRequestDto 생성할 계좌 정보 요청 DTO
     * @return 생성된 계좌 정보를 담은 AccountResponseDto
     */
    AccountResponseDto createLinkedAccount(Long userId, CreateAccountRequestDto createAccountRequestDto);
}
