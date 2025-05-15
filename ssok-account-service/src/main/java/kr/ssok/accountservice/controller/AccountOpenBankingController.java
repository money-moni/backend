package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.request.AccountOwnerRequestDto;
import kr.ssok.accountservice.dto.response.AccountOwnerResponseDto;
import kr.ssok.accountservice.dto.response.AllAccountsResponseDto;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.service.AccountOpenBankingService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 오픈뱅킹 관련 API 요청을 처리하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/accounts/openbank")
@RequiredArgsConstructor
public class AccountOpenBankingController {
    private final AccountOpenBankingService accountOpenBankingService;

    /**
     * 오픈뱅킹 서버로부터 사용자의 전체 계좌 목록을 조회합니다.
     *
     * @param userId Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @return 조회된 전체 계좌 목록을 담은 {@link BaseResponse}
     */
    @PostMapping
    public ResponseEntity<BaseResponse<List<AllAccountsResponseDto>>> getOpenBankingAccounts(
            @RequestHeader("X-User-Id") String userId) {
        List<AllAccountsResponseDto> result = this.accountOpenBankingService.fetchAllAccountsFromOpenBanking(Long.parseLong(userId));

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }

    /**
     * 오픈뱅킹 서버로 부터 예금주의 실명을 조회합니다.
     *
     * @param accountOwnerRequestDto 실명 확인 요청에 필요한 계좌 정보
     * @return 예금주의 실명 정보를 담은 {@link BaseResponse}
     */
    @PostMapping("/verify-name")
    public ResponseEntity<BaseResponse<AccountOwnerResponseDto>> getOpenBankingAccountOwner(
            @RequestBody AccountOwnerRequestDto accountOwnerRequestDto) {
        AccountOwnerResponseDto result = this.accountOpenBankingService.fetchAccountOwnerFromOpenBanking(accountOwnerRequestDto);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_GET_SUCCESS, result));
    }
}
