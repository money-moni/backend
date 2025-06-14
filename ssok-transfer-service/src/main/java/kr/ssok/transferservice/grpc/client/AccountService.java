package kr.ssok.transferservice.grpc.client;

import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.response.AccountResponseDto;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;

import java.util.List;

public interface AccountService {
    AccountResponseDto getAccountInfo(Long accountId, String userId);
    AccountIdResponseDto getAccountId(String accountNumber);
    List<AccountIdResponseDto> getAccountIdsByUserId(String userId);
    PrimaryAccountResponseDto getPrimaryAccountInfo(String userId);
}