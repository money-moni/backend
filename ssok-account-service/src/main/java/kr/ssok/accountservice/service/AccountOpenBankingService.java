package kr.ssok.accountservice.service;

import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;

import java.util.List;

/**
 * 오픈뱅킹 연동 기능을 정의하는 Service 인터페이스
 *
 * <p>주요 기능:
 * <ul>
 *     <li>전체 계좌 목록 조회</li>
 *     <li>계좌 실명 확인</li>
 *     <li>계좌 잔액 조회</li>
 * </ul>
 * </p>
 */
public interface AccountOpenBankingService {

    /**
     * 사용자 ID를 기반으로 오픈뱅킹 서버에 전체 계좌 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 전체 계좌 목록 응답 DTO 리스트
     */
    List<AllAccountsResponseDto> fetchAllAccountsFromOpenBanking(Long userId);

    /**
     * 오픈뱅킹 서버에 계좌 실명 확인 요청을 보냅니다.
     *
     * @param accountOwnerRequestDto 실명 조회 요청 DTO
     * @return 실명 확인 결과 DTO
     */
    AccountOwnerResponseDto fetchAccountOwnerFromOpenBanking(AccountOwnerRequestDto accountOwnerRequestDto);

    /**
     * 오픈뱅킹 서버에 계좌 잔액 조회 요청을 보냅니다.
     *
     * @param openBankingAccountBalanceRequestDto 잔액 조회 요청 DTO
     * @return 잔액 조회 결과 DTO
     */
    OpenBankingAccountBalanceResponseDto fetchAccountBalanceFromOpenBanking(OpenBankingAccountBalanceRequestDto openBankingAccountBalanceRequestDto);
}
