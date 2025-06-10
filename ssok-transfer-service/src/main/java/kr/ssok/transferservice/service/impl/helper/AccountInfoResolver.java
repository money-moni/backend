package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.client.dto.response.AccountResponseDto;
import kr.ssok.transferservice.grpc.client.AccountServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountInfoResolver {

    private final AccountServiceClient accountServiceClient;

    /**
     * 출금 계좌 번호 조회 메서드
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 출금 계좌 번호
     */
    public String findSendAccountNumber(Long accountId, Long userId) {
//        BaseResponse<AccountResponseDto> response = this.accountServiceClient.getAccountInfo(accountId, userId.toString());
//
//        if (!response.getIsSuccess()) {
//            log.warn("출금 계좌 조회 실패: {}", response.getMessage());
//            throw new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
//        }
        AccountResponseDto response = this.accountServiceClient.getAccountInfo(accountId, userId.toString());
        return response.getAccountNumber();
    }
}