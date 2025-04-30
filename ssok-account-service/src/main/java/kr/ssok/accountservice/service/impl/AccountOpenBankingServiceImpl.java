package kr.ssok.accountservice.service.impl;

import kr.ssok.accountservice.client.OpenBankingClient;
import kr.ssok.accountservice.client.UserServiceClient;
import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountBalanceRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAccountOwnerRequestDto;
import kr.ssok.accountservice.dto.request.openbanking.OpenBankingAllAccountsRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.openbanking.OpenBankingAllAccountsResponseDto;
import kr.ssok.accountservice.dto.response.userservice.UserInfoResponseDto;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 오픈뱅킹과의 연동 기능을 제공하는 서비스 구현 클래스
 *
 * <p>유저 서비스 및 오픈뱅킹 서비스와의 통신을 통해 전체 계좌 조회, 실명 조회, 잔액 조회 기능을 제공합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOpenBankingServiceImpl implements AccountOpenBankingService {
    private final OpenBankingClient openBankingClient;
    private final UserServiceClient userServiceClient;

    /**
     * 오픈뱅킹 서버로부터 전체 계좌 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 전체 계좌 목록 응답 DTO 리스트
     * @throws AccountException 유저 정보 조회 또는 계좌 조회 실패 시 발생
     */
    @Override
    public List<AllAccountsResponseDto> fetchAllAccountsFromOpenBanking(Long userId) {
        BaseResponse<UserInfoResponseDto> userInfoResponse = this.userServiceClient.sendUserInfoRequest(userId);

        if (userInfoResponse == null || userInfoResponse.getResult() == null) {
            log.warn("[OPENBANKING] 사용자 정보 조회 실패: userId={}", userId);
            throw new AccountException(AccountResponseStatus.USER_INFO_NOT_FOUND);
        }

        OpenBankingAllAccountsRequestDto requestDto =
                OpenBankingAllAccountsRequestDto.from(userInfoResponse.getResult());

        BaseResponse<List<OpenBankingAllAccountsResponseDto>> response =
                this.openBankingClient.sendAllAccountsRequest(requestDto);

        if (response == null || response.getResult() == null) {
            log.warn("[OPENBANKING] 전체 계좌 조회 실패: userId={}, username={}", userId, requestDto.getUsername());
            throw new AccountException(AccountResponseStatus.OPENBANKING_ACCOUNT_LIST_FAILED);
        }

        return response.getResult()
                .stream()
                .map(AllAccountsResponseDto::from)
                .toList();
    }

    /**
     * 오픈뱅킹 서버로부터 실명 정보를 조회합니다.
     *
     * @param accountOwnerRequestDto 실명 조회 요청 DTO
     * @return 실명 확인 결과 DTO
     * @throws AccountException 실명 조회 실패 시 발생
     */
    @Override
    public AccountOwnerResponseDto fetchAccountOwnerFromOpenBanking(AccountOwnerRequestDto accountOwnerRequestDto) {
        OpenBankingAccountOwnerRequestDto requestDto = OpenBankingAccountOwnerRequestDto.from(accountOwnerRequestDto);

        BaseResponse<OpenBankingAccountOwnerResponseDto> response =
                this.openBankingClient.sendAccountOwnerRequest(requestDto);

        if (response == null || response.getResult() == null) {
            log.warn("[OPENBANKING] 실명 조회 실패: accountNumber={}, bankCode={}",
                    requestDto.getAccountNumber(), requestDto.getBankCode());
            throw new AccountException(AccountResponseStatus.OPENBANKING_OWNER_LOOKUP_FAILED);
        }

        return AccountOwnerResponseDto.from(response.getResult(), requestDto.getAccountNumber());
    }

    /**
     * 오픈뱅킹 서버로부터 계좌 잔액을 조회합니다.
     *
     * @param requestDto 잔액 조회 요청 DTO
     * @return 잔액 조회 결과 DTO
     * @throws AccountException 잔액 조회 실패 시 발생
     */
    @Override
    public OpenBankingAccountBalanceResponseDto fetchAccountBalanceFromOpenBanking(OpenBankingAccountBalanceRequestDto requestDto) {
        BaseResponse<OpenBankingAccountBalanceResponseDto> response =
                this.openBankingClient.sendAccountBalanceRequest(requestDto);

        if (response == null || response.getResult() == null) {
            log.warn("[OPENBANKING] 잔액 조회 실패: accountNumber={}, bankCode={}",
                    requestDto.getAccountNumber(), requestDto.getBankCode());
            throw new AccountException(AccountResponseStatus.OPENBANKING_BALANCE_LOOKUP_FAILED);
        }

        return response.getResult();
    }
}
