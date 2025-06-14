package kr.ssok.transferservice.grpc.client;

import kr.ssok.common.grpc.account.*;
import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.response.AccountResponseDto;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountServiceClient implements AccountService {
    private final AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub;

    /**
     * 계좌 ID와 사용자 ID를 기반으로 계좌번호를 조회
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 계좌 번호가 포함된 응답 DTO
     */
    @Override
    public AccountResponseDto getAccountInfo(Long accountId, String userId) {
        AccountInfoRequest request = AccountInfoRequest.newBuilder()
                .setAccountId(accountId)
                .setUserId(userId)
                .build();
        AccountInfoResponse response =
                this.accountServiceBlockingStub.getAccountInfo(request);

        return AccountResponseDto.builder()
                .accountNumber(response.getAccountNumber())
                .build();
    }

    /**
     * 계좌번호를 기반으로 계좌 ID를 조회
     *
     * @param accountNumber 조회할 계좌번호
     * @return 계좌 ID, 사용자 ID가 포함된 응답 DTO
     * */
    @Override
    public AccountIdResponseDto getAccountId(String accountNumber) {
        AccountNumberRequest request = AccountNumberRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

        AccountIdResponse response =
                this.accountServiceBlockingStub.getAccountIdByAccountNumber(request);

        return AccountIdResponseDto.builder()
                .accountId(response.getAccountId())
                .userId(response.getUserId())
                .build();
    }

    /**
     * 사용자 ID를 기반으로 해당 사용자의 모든 계좌 ID 리스트를 조회
     *
     * @param userId 사용자 ID
     * @return 계좌 ID가 포함된 DTO를 리스트로 반환
     */
    @Override
    public List<AccountIdResponseDto> getAccountIdsByUserId(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        AccountIdsResponse response =
                this.accountServiceBlockingStub.getAccountIdsByUserId(request);

        return response.getAccountIdList().stream()
                .map(accountId -> AccountIdResponseDto.builder()
                        .accountId(accountId)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 주 계좌 정보를 조회
     *
     * @param userId 사용자 ID를 담고 있는 헤더 값 (X-User-Id)
     * @return 사용자 주계좌 정보가 포함된 응답 DTO
     */
    @Override
    public PrimaryAccountResponseDto getPrimaryAccountInfo(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId)
                .build();

        PrimaryAccountInfoResponse response =
                this.accountServiceBlockingStub.getPrimaryAccountInfo(request);

        return PrimaryAccountResponseDto.builder()
                .accountId(response.getAccountId())
                .accountNumber(response.getAccountNumber())
                .bankCode(response.getBankCode())
                .username(response.getUsername())
                .build();
    }

}
