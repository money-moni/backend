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
import kr.ssok.accountservice.util.AccountIdentifierUtil;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
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
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 오픈뱅킹 서버와 사용자 서비스로부터 사용자 정보를 기반으로 전체 연동 계좌 목록을 조회합니다.
     *
     * <ul>
     *     <li>유저 서비스로부터 사용자 정보를 조회합니다.</li>
     *     <li>해당 정보를 기반으로 오픈뱅킹 서버에 전체 계좌 조회 요청을 보냅니다.</li>
     *     <li>조회된 계좌 정보를 Redis에 캐시하여 계좌 유효성 검사에 활용합니다 (TTL 5분).</li>
     *     <li>오픈뱅킹 응답 데이터를 도메인 응답 DTO로 변환하여 반환합니다.</li>
     * </ul>
     *
     * @param userId 사용자 ID
     * @return 전체 계좌 정보 DTO
     * @throws AccountException 사용자 정보 또는 오픈뱅킹 응답이 유효하지 않을 경우 발생
     */
    @Override
    public List<AllAccountsResponseDto> fetchAllAccountsFromOpenBanking(Long userId) {
        BaseResponse<UserInfoResponseDto> userInfoResponse = this.userServiceClient.sendUserInfoRequest(userId.toString());

        if (userInfoResponse == null || userInfoResponse.getResult() == null) {
            log.warn("[USERSERVICE] 사용자 정보 조회 실패: userId={}", userId);
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

        // 계좌 유효성 검사를 위한 로직 - redis에 오픈뱅킹으로 받은 정보 캐싱
        cacheAvailableAccountSet(userId, response.getResult());

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

    private void cacheAvailableAccountSet(Long userId, List<OpenBankingAllAccountsResponseDto> accounts) {
        String redisKey = AccountIdentifierUtil.buildLookupKey(userId);

        redisTemplate.opsForSet().add(redisKey,
                accounts.stream()
                        .map(dto -> AccountIdentifierUtil.buildLookupValue(
                                dto.getBankCode(),
                                dto.getAccountNumber(),
                                dto.getAccountTypeCode()))
                        .distinct()
                        .toArray(String[]::new));

        redisTemplate.expire(redisKey, Duration.ofMinutes(5)); // TTL 5분
    }
}
