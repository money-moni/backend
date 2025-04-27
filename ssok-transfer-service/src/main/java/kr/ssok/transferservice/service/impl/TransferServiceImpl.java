package kr.ssok.transferservice.service.impl;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.OpenBankingClient;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 송금 서비스의 실제 구현체
 */
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountServiceClient accountServiceClient;
    private final OpenBankingClient openBankingClient;
    private final TransferHistoryRepository transferHistoryRepository;

    /**
     * 계좌 서비스에서 출금 계좌번호를 조회하고(유효성 및 보안 검증),
     * 오픈뱅킹 API를 통해 송금을 요청한 후,
     * 송금 이력을 저장하고 결과를 반환
     *
     * @param userId 사용자 ID
     * @param dto    요청 DTO
     * @return 송금 응답 DTO
     */
    @Override
    public TransferResponseDto transfer(Long userId, TransferRequestDto dto) {
        // 0. 송금 금액이 0보다 큰지 검증
        if (dto.getAmount() == null || dto.getAmount() <= 0) {
            throw new TransferException(TransferResponseStatus.INVALID_TRANSFER_AMOUNT);
        }

        // 1. 계좌 서비스에서 출금 계좌번호 조회
        BaseResponse<AccountServiceClient.AccountResponse.Result> accountResponse =
                this.accountServiceClient.getAccountInfo(dto.getSendAccountId(), userId);

        if (!accountResponse.getIsSuccess()) {
            throw new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
        }

        String sendAccountNumber = accountResponse.getResult().getAccountNumber();

        // 2. 오픈뱅킹 송금 요청
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sendAccountNumber", sendAccountNumber);
        requestBody.put("sendBankCode", dto.getSendBankCode());
        requestBody.put("recvAccountNumber", dto.getRecvAccountNumber());
        requestBody.put("recvBankCode", dto.getRecvBankCode());
        requestBody.put("amount", dto.getAmount());

        BaseResponse<Object> transferResponse = this.openBankingClient.sendTransferRequest(requestBody);

        if (!transferResponse.getIsSuccess()) {
            throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
        }

        this.transferHistoryRepository.save(
                TransferHistory.builder()
                        .accountId(dto.getSendAccountId())
                        .counterpartAccount(dto.getRecvAccountNumber())
                        .counterpartName(dto.getRecvName())
                        .transferType(TransferType.WITHDRAWAL)
                        .transferMoney(dto.getAmount())
                        .currencyCode(CurrencyCode.KRW)
                        .transferMethod(TransferMethod.GENERAL)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return TransferResponseDto.builder()
                .sendAccountId(dto.getSendAccountId())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .amount(dto.getAmount())
                .build();
    }
}
