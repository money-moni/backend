package kr.ssok.accountservice.grpc.server;

import io.grpc.stub.StreamObserver;
import kr.ssok.accountservice.dto.response.bluetoothservice.PrimaryAccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdsResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.PrimaryAccountInfoResponseDto;
import kr.ssok.accountservice.service.AccountInternalService;
import kr.ssok.common.grpc.account.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AccountGrpcInternalServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountInternalService accountInternalService;

    @Override
    public void getAccountInfo(AccountInfoRequest request, StreamObserver<AccountInfoResponse> responseObserver) {
        AccountInfoResponseDto result =
                this.accountInternalService.findAccountByUserIdAndAccountId(Long.parseLong(request.getUserId()), request.getAccountId());

        AccountInfoResponse response = AccountInfoResponse.newBuilder()
                .setAccountId(result.getAccountId())
                .setUserId(result.getUserId())
                .setAccountNumber(result.getAccountNumber())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAccountIdByAccountNumber(AccountNumberRequest request, StreamObserver<AccountIdResponse> responseObserver) {
        AccountIdResponseDto result =
                this.accountInternalService.findAccountIdByAccountNumber(request.getAccountNumber());

        AccountIdResponse response = AccountIdResponse.newBuilder()
                .setAccountId(result.getAccountId())
                .setUserId(result.getUserId())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getAccountIdsByUserId(UserIdRequest request, StreamObserver<AccountIdsResponse> responseObserver) {
        List<AccountIdsResponseDto> result =
                this.accountInternalService.findAllAccountIds(Long.parseLong(request.getUserId()));

        List<Long> accountIdList = result.stream()
                .map(AccountIdsResponseDto::getAccountId)
                .toList();

        AccountIdsResponse response = AccountIdsResponse.newBuilder()
                .addAllAccountId(accountIdList)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPrimaryAccountInfo(UserIdRequest request, StreamObserver<PrimaryAccountInfoResponse> responseObserver) {
        PrimaryAccountInfoResponseDto result =
                this.accountInternalService.findPrimaryAccountByUserId(Long.parseLong(request.getUserId()));

        PrimaryAccountInfoResponse response = PrimaryAccountInfoResponse.newBuilder()
                .setAccountId(result.getAccountId())
                .setAccountNumber(result.getAccountNumber())
                .setBankCode(result.getBankCode())
                .setUsername(result.getUsername())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPrimaryAccountBalance(UserIdRequest request, StreamObserver<PrimaryAccountBalanceResponse> responseObserver) {
        PrimaryAccountBalanceResponseDto result =
                this.accountInternalService.findPrimaryAccountBalanceByUserId(Long.parseLong(request.getUserId()));

        PrimaryAccountBalanceResponse response = PrimaryAccountBalanceResponse.newBuilder()
                .setAccountId(result.getAccountId())
                .setAccountNumber(result.getAccountNumber())
                .setBankCode(result.getBankCode())
                .setBalance(result.getBalance())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

}
