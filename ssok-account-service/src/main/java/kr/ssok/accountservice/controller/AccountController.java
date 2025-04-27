package kr.ssok.accountservice.controller;

import kr.ssok.accountservice.dto.request.AccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAccount(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody AccountRequestDto accountRequestDto) {
        AccountResponseDto accountResponseDto = this.accountService.createLinkedAccount(Long.parseLong(userId), accountRequestDto);

        // TODO. common responseBody를 불러오는 것으로 수정 예졍
        return ResponseEntity.ok().body(Map.of("isSuccess",true,"code", 200,"message","계좌 등록을 완료했습니다.", "result", accountResponseDto));
    }


}
