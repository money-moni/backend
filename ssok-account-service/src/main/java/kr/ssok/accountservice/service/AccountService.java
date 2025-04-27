package kr.ssok.accountservice.service;

import kr.ssok.accountservice.dto.request.AccountRequestDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;

public interface AccountService {
    AccountResponseDto createLinkedAccount(Long userId, AccountRequestDto accountRequestDto);
}
