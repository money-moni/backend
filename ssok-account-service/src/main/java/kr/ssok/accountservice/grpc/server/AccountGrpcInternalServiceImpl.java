package kr.ssok.accountservice.grpc.server;

import io.grpc.stub.StreamObserver;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountIdsResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.AccountInfoResponseDto;
import kr.ssok.accountservice.dto.response.transferservice.PrimaryAccountInfoResponseDto;
import kr.ssok.accountservice.exception.AccountException;
import kr.ssok.accountservice.exception.AccountResponseStatus;
import kr.ssok.accountservice.exception.grpc.GrpcExceptionUtil;
import kr.ssok.accountservice.service.AccountInternalService;
import kr.ssok.common.grpc.account.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountGrpcInternalServiceImpl extends AccountServiceGrpc.AccountServiceImplBase {
    private final AccountInternalService accountInternalService;

    @Override
    public void getAccountInfo(AccountInfoRequest request, StreamObserver<AccountInfoResponse> responseObserver) {
        try {
            AccountInfoResponseDto result =
                    this.accountInternalService.findAccountByUserIdAndAccountId(Long.parseLong(request.getUserId()), request.getAccountId());

            AccountInfoResponse response = AccountInfoResponse.newBuilder()
                    .setAccountId(result.getAccountId())
                    .setUserId(result.getUserId())
                    .setAccountNumber(result.getAccountNumber())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountException ex) {
            log.error("[gRPC][getAccountInfo] UserException: {}", ex.getStatus());
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            log.error("[gRPC][getAccountInfo] Unexpected error", ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(AccountResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void getAccountIdByAccountNumber(AccountNumberRequest request, StreamObserver<AccountIdResponse> responseObserver) {
        try {
            AccountIdResponseDto result =
                    this.accountInternalService.findAccountIdByAccountNumber(request.getAccountNumber());

            AccountIdResponse response = AccountIdResponse.newBuilder()
                    .setAccountId(result.getAccountId())
                    .setUserId(result.getUserId())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (AccountException ex) {
            log.error("[gRPC][getAccountIdByAccountNumber] UserException: {}", ex.getStatus());
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            log.error("[gRPC][getAccountIdByAccountNumber] Unexpected error", ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(AccountResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void getAccountIdsByUserId(UserIdRequest request, StreamObserver<AccountIdsResponse> responseObserver) {
        try {
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
        } catch (AccountException ex) {
            log.error("[gRPC][getAccountIdsByUserId] UserException: {}", ex.getStatus());
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            log.error("[gRPC][getAccountIdsByUserId] Unexpected error", ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(AccountResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void getPrimaryAccountInfo(UserIdRequest request, StreamObserver<PrimaryAccountInfoResponse> responseObserver) {
        try {
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
        } catch (AccountException ex) {
            log.error("[gRPC][getPrimaryAccountInfo] UserException: {}", ex.getStatus());
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(ex.getStatus()));
        } catch (Exception ex) {
            log.error("[gRPC][getPrimaryAccountInfo] Unexpected error", ex);
            responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(AccountResponseStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void getPrimaryAccountBalance(UserIdRequest request, StreamObserver<PrimaryAccountBalanceResponse> responseObserver) {
        this.accountInternalService.findPrimaryAccountBalanceByUserId(Long.parseLong(request.getUserId()))
                .thenAccept(result -> {
                    PrimaryAccountBalanceResponse response = PrimaryAccountBalanceResponse.newBuilder()
                            .setAccountId(result.getAccountId())
                            .setAccountNumber(result.getAccountNumber())
                            .setBankCode(result.getBankCode())
                            .setBalance(result.getBalance())
                            .build();

                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                })
                .exceptionally(e -> {
                    Throwable cause = (e instanceof java.util.concurrent.CompletionException && e.getCause() != null) ? e.getCause() : e;
                    if (cause instanceof AccountException accEx) {
                        log.error("[gRPC][getPrimaryAccountInfo] UserException: {}", accEx.getStatus());
                        responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(accEx.getStatus()));
                    } else {
                        log.error("[gRPC][getPrimaryAccountInfo] Unexpected error", cause);
                        responseObserver.onError(GrpcExceptionUtil.toStatusRuntimeException(AccountResponseStatus.INTERNAL_SERVER_ERROR));
                    }
                    return null;
                });
    }

}
