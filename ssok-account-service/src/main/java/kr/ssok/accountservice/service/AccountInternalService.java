package kr.ssok.accountservice.service;

import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;

import java.util.List;

/**
 * MSA 내 다른 서비스에서 내부 호출을 위한 계좌 조회 기능을 정의하는 Service 인터페이스
 *
 * <p>주요 기능:
 * <ul>
 *     <li>사용자 ID와 계좌 ID로 계좌 상세 조회</li>
 *     <li>계좌번호로 계좌 ID 조회</li>
 *     <li>사용자 ID로 보유한 모든 계좌 ID 조회</li>
 * </ul>
 * </p>
 */
public interface AccountInternalService {

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 계좌 상세 정보를 담은 AccountInfoResponseDto
     */
    AccountInfoResponseDto findAccountByUserIdAndAccountId(Long userId, Long accountId);

    /**
     * 계좌번호에 해당하는 계좌 ID를 조회합니다.
     *
     * @param accountNumber 조회할 계좌번호
     * @return 계좌 ID 정보를 담은 AccountIdResponseDto
     */
    AccountIdResponseDto findAccountIdByAccountNumber(String accountNumber);

    /**
     * 사용자 ID에 해당하는 모든 연동 계좌 ID 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 계좌 ID 목록을 담은 List<AccountIdResponseDto>
     */
    List<AccountIdResponseDto> findAllAccountIds(Long userId);
}
