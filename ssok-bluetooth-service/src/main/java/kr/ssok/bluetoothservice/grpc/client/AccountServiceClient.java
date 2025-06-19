package kr.ssok.bluetoothservice.grpc.client;

import kr.ssok.bluetoothservice.client.dto.AccountInfoDto;
import kr.ssok.common.grpc.account.AccountServiceGrpc;
import kr.ssok.common.grpc.account.PrimaryAccountBalanceResponse;
import kr.ssok.common.grpc.account.UserIdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountServiceClient {
    private final AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;

    public AccountInfoDto getPrimaryAccount(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        PrimaryAccountBalanceResponse response =
                this.accountServiceBlockingStub.getPrimaryAccountBalance(request);

        return AccountInfoDto.builder()
                .accountId(response.getAccountId())
                .accountNumber(response.getAccountNumber())
                .bankCode(response.getBankCode())
                .balance(response.getBalance())
                .build();
    }
}
