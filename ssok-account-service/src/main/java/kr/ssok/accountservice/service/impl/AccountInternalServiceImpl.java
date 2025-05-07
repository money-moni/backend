package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
import kr.ssok.accountservice.entity.LinkedAccount;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.repository.AccountRepository;
import kr.ssok.accountservice.service.AccountInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
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

    /**
     * 사용자 ID와 계좌 ID에 해당하는 연동 계좌 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param accountId 조회할 계좌 ID
     * @return 조회된 계좌 정보를 담은 {@link AccountInfoResponseDto}
     * @throws AccountException 해당 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public AccountInfoResponseDto findAccountByUserIdAndAccountId(Long userId, Long accountId) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountIdAndUserId(accountId, userId)
                .orElseThrow(() -> {
                    log.warn("[GET] Account not found: accountId={}, userId={}", accountId, userId);
                    return new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
                });

        return AccountInfoResponseDto.from(linkedAccount);
    }

    /**
     * 계좌번호에 해당하는 계좌 ID 정보를 조회합니다.
     *
     * @param accountNumber 조회할 계좌번호
     * @return 계좌 ID를 포함한 응답 DTO {@link AccountIdResponseDto}
     * @throws AccountException 해당 계좌가 존재하지 않는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public AccountIdResponseDto findAccountIdByAccountNumber(String accountNumber) {
        LinkedAccount linkedAccount = this.accountRepository.findByAccountNumber(accountNumber)
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
     * @return 사용자의 연동 계좌 ID 목록을 담은 {@link List}<{@link AccountIdResponseDto}>
     * @throws AccountException 등록된 계좌가 하나도 없는 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountIdResponseDto> findAllAccountIds(Long userId) {
        List<LinkedAccount> linkedAccounts = this.accountRepository.findByUserId(userId);

        if (CollectionUtils.isEmpty(linkedAccounts)) {
            log.warn("[GET] Account not found: userId={}", userId);
            throw new AccountException(AccountResponseStatus.ACCOUNT_NOT_FOUND);
        }

        return linkedAccounts.stream()
                .map(AccountIdResponseDto::from)
                .collect(Collectors.toList());
    }
}
