package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.request.CreateAccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.service.AccountService;
import kr.ssok.common.exception.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 계좌 관련 API 요청을 처리하는 REST 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    /**
     * 계좌 생성 요청을 처리합니다.
     * <p>
     * 클라이언트로부터 계좌 생성 요청을 받아 신규 연동 계좌를 생성하고,
     * 생성된 계좌 정보를 반환합니다.
     * </p>
     *
     * @param userId                  Gateway에서 전달된 사용자 ID (요청 헤더 "X-User-Id")
     * @param createAccountRequestDto 클라이언트로부터 전달받은 계좌 생성 요청 데이터
     * @return 생성된 계좌 정보를 담은 {@link BaseResponse}
     */
    @PostMapping
    public ResponseEntity<BaseResponse<AccountResponseDto>> createAccount(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody CreateAccountRequestDto createAccountRequestDto) {
        AccountResponseDto result = this.accountService.createLinkedAccount(Long.parseLong(userId), createAccountRequestDto);

        return ResponseEntity.ok().body(new BaseResponse<>(AccountResponseStatus.ACCOUNT_CREATE_SUCCESS, result));
    }


}
