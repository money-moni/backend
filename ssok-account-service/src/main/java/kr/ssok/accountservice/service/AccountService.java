package kr.ssok.accountservice.service;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.request.UpdateAliasRequestDto;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;

import java.util.List;

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

    /**
     * 사용자 ID에 해당하는 모든 연동 계좌 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 조회된 연동 계좌 목록을 담은 List<AccountBalanceResponseDto>
     */
    List<AccountBalanceResponseDto> findAllAccounts(Long userId);

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 상세 조회합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 조회된 연동 계좌 정보를 담은 AccountBalanceResponseDto
     */
    AccountBalanceResponseDto findAccountById(Long userId, Long accountId);

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 삭제할 계좌 ID
     * @return 삭제된 계좌의 기본 정보를 담은 {@link AccountResponseDto}
     */
    AccountResponseDto deleteLinkedAccount(Long userId, Long accountId);

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌의 별칭(alias)을 수정합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 별칭을 수정할 계좌 ID
     * @param updateAliasRequestDto 별칭 수정 요청 DTO (수정할 alias 포함)
     * @return 별칭이 수정된 계좌 정보를 담은 {@link AccountResponseDto}
     */
    AccountResponseDto updateLinkedAccountAlias(Long userId, Long accountId, UpdateAliasRequestDto updateAliasRequestDto);

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌를 주계좌(primary account)로 설정합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 주계좌로 설정할 계좌 ID
     * @return 주계좌로 변경된 계좌 정보를 담은 {@link AccountResponseDto}
     */
    AccountResponseDto updatePrimaryLinkedAccount(Long userId, Long accountId);
}
