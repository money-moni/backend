package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.response.bluetoothservice.PrimaryAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdsResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.PrimaryAccountInfoResponseDto;
import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.grpc.client.UserServiceClient;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountInternalService;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
     * 내부 서비스 간 연동을 위한 계좌 조회 기능을 제공하는 구현 클래스
 *
 * <p>계좌 ID, 계좌번호, 사용자 ID 기반의 계좌 정보 조회 기능을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountInternalServiceImpl implements AccountInternalService {
    private final AccountRepository accountRepository;
    private final UserServiceClient userServiceClient;
    private final AccountOpenBankingService accountOpenBankingService;


    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 조회된 계좌 정보를 담은 DTO
     * @throws AccountException 해당 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public AccountInfoResponseDto findAccountByUserIdAndAccountId(Long userId, Long accountId) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserIdAndIsDeletedFalse(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        return AccountInfoResponseDto.from(linkedAccount);
    }

    /**
     * 계좌번호에 해당하는 계좌 ID, 유저 ID 정보를 조회합니다.
     *
     * @param accountNumber 조회할 계좌번호
     * @return 계좌 ID, 유저 ID를 포함한 응답 DTO
     * @throws AccountException 해당 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public AccountIdResponseDto findAccountIdByAccountNumber(String accountNumber) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountNumberAndIsDeletedFalse(accountNumber)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountNumber={}", accountNumber);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        return AccountIdResponseDto.from(linkedAccount);
    }

    /**
     * 사용자 ID에 해당하는 모든 계좌의 ID 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 연동 계좌 ID 목록을 담은 DTO
     * @throws AccountException 등록된 계좌가 하나도 없는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountIdsResponseDto> findAllAccountIds(Long userId) {
        List<LinkedAccount> linkedAccounts = this.accountRepository.findByUserIdAndIsDeletedFalse(userId);

        if (CollectionUtils.isEmpty(linkedAccounts)) {
            log.warn("[GET] Account not found: userId={}", userId);
            throw new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
        }

        return linkedAccounts.stream()
                .map(AccountIdsResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 ID에 해당하는 대표 계좌 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 대표 계좌 정보와 사용자 이름을 포함한 DTO
     * @throws AccountException 대표 계좌가 존재하지 않거나, 사용자 정보 조회에 실패한 경우 발생
     */
    @Override
    public PrimaryAccountInfoResponseDto findPrimaryAccountByUserId(Long userId) {
        LinkedAccount linkedAccount = this.accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: userId={}", userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        UserInfoResponseDto userInfoResponse = this.userServiceClient.getUserInfo(userId.toString());
//        BaseResponse<UserInfoResponseDto> userInfoResponse = this.userServiceClient.sendUserInfoRequest(userId.toString());
//        if (userInfoResponse == null || userInfoResponse.getResult() == null) {
//            log.warn("[USERSERVICE] 사용자 정보 조회 실패: userId={}", userId);
//            throw new AccountException(AccountResponseStatus.USER_INFO_NOT_FOUND);
//        }

        return PrimaryAccountInfoResponseDto.from(linkedAccount, userInfoResponse.getUsername());
    }

    /**
     * 사용자 ID에 해당하는 대표 계좌 잔액 정보를 비동기로 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 대표 계좌 정보와 잔액 정보를 포함한 DTO
     * @throws AccountException 대표 계좌가 존재하지 않거나, 잔액 정보 조회에 실패한 경우 발생
     */
    @Override
    public CompletableFuture<PrimaryAccountBalanceResponseDto> findPrimaryAccountBalanceByUserId(Long userId) {
        LinkedAccount linkedAccount = this.accountRepository.findByUserIdAndIsPrimaryAccountTrueAndIsDeletedFalse(userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: userId={}", userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        OpenBankingAccountBalanceRequestDto requestDto =
                OpenBankingAccountBalanceRequestDto.from(linkedAccount);

        return accountOpenBankingService.fetchAccountBalanceFromOpenBanking(requestDto)
                .thenApply(balanceDto -> PrimaryAccountBalanceResponseDto.from(
                        linkedAccount,
                        balanceDto != null && balanceDto.getBalance() != null ? balanceDto.getBalance() : -1L))
                .exceptionally(e -> {
                    log.error("[OPENBANKING][비동기] 잔액 조회 에러: userId={}", userId, e);
                    return PrimaryAccountBalanceResponseDto.from(linkedAccount, -1L);
                });
    }
}
