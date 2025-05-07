package kr.ssok.transferservice.service.impl;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.OpenBankingClient;
import kr.ssok.transferservice.client.dto.AccountIdResponse;
import kr.ssok.transferservice.client.dto.AccountResponse;
import kr.ssok.transferservice.dto.request.OpenBankingTransferRequestDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 송금 서비스의 실제 구현체
 */
@Slf4j
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
    @Transactional
    @Override
    public TransferResponseDto transfer(Long userId, TransferRequestDto dto) {
        // 0. 송금 금액이 0보다 큰지 검증
        validateTransferAmount(dto.getAmount());

        // 1. 계좌 서비스에서 출금 계좌번호 조회
        String sendAccountNumber = findSendAccountNumber(dto.getSendAccountId(), userId);

        // 2. 오픈뱅킹 송금 요청
        requestOpenBankingTransfer(sendAccountNumber, dto);

        // 3. 출금 내역 저장
        saveTransferHistory(
                dto.getSendAccountId(),
                dto.getRecvAccountNumber(),
                dto.getRecvName(),
                TransferType.WITHDRAWAL,
                dto.getAmount(),
                CurrencyCode.KRW,
                TransferMethod.GENERAL
        );

        // 4. 상대방 계좌번호로 계좌 ID 조회 후, 입금 이력 추가 저장 (SSOK 유저인 경우만)
        saveDepositHistoryIfReceiverExists(sendAccountNumber, dto);

        // 5. 최종 송금 응답 반환
        return TransferResponseDto.builder()
                .sendAccountId(dto.getSendAccountId())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .amount(dto.getAmount())
                .build();
    }

    /**
     * 송금 금액이 0보다 큰지 검증
     *
     * @param amount 송금 금액
     */
    private void validateTransferAmount(Long amount) {
        if (amount == null || amount <= 0) {
            throw new TransferException(TransferResponseStatus.INVALID_TRANSFER_AMOUNT);
        }
    }

    /**
     * 본인의 출금 계좌번호 조회
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 출금 계좌번호
     */
    private String findSendAccountNumber(Long accountId, Long userId) {
        BaseResponse<AccountResponse> response =
                this.accountServiceClient.getAccountInfo(accountId, userId);

        if (!response.getIsSuccess()) {
            log.error("계좌 조회 실패: {}", response.getMessage());
            throw new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
        }

        return response.getResult().getAccountNumber();
    }

    /**
     * 오픈뱅킹 송금 요청
     *
     * @param sendAccountNumber 출금 계좌번호
     * @param dto 송금 요청 DTO
     */
    private void requestOpenBankingTransfer(String sendAccountNumber, TransferRequestDto dto) {
        OpenBankingTransferRequestDto request = OpenBankingTransferRequestDto.builder()
                .sendAccountNumber(sendAccountNumber)
                .sendBankCode(dto.getSendBankCode())
                .sendName(dto.getSendName())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .recvBankCode(dto.getRecvBankCode())
                .recvName(dto.getRecvName())
                .amount(dto.getAmount())
                .build();

        BaseResponse<Object> response = this.openBankingClient.sendTransferRequest(request);

        if (!response.getIsSuccess()) {
            log.error("오픈뱅킹 송금 실패: {}", response.getMessage());
            throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
        }
    }

    /**
     * 송금 이력 저장 (출금/입금)
     *
     * @param accountId 계좌 ID
     * @param counterpartAccount 상대방 계좌번호
     * @param counterpartName 상대방 이름
     * @param transferType 송금 유형 (출금/입금)
     * @param amount 금액
     * @param currencyCode 통화 유형
     * @param transferMethod 송금 유형
     */
    private void saveTransferHistory(Long accountId, String counterpartAccount, String counterpartName,
                                     TransferType transferType, Long amount, CurrencyCode currencyCode, TransferMethod transferMethod) {
        // 상대방 입금 내역 저장
        this.transferHistoryRepository.save(
                TransferHistory.builder()
                        .accountId(accountId)
                        .counterpartAccount(counterpartAccount)
                        .counterpartName(counterpartName)
                        .transferType(transferType)
                        .transferMoney(amount)
                        .currencyCode(currencyCode)
                        .transferMethod(transferMethod)
                        .build()
        );
    }

    /**
     * 상대방 계좌 ID가 존재하면 입금 이력 저장
     *
     * @param sendAccountNumber 송금자 계좌번호
     * @param dto 송금 요청 DTO
     */
    private void saveDepositHistoryIfReceiverExists(String sendAccountNumber, TransferRequestDto dto) {
        BaseResponse<AccountIdResponse> response =
                this.accountServiceClient.getAccountId(dto.getRecvAccountNumber());

        if (response.getIsSuccess()
                && response.getCode() == 2000
                && response.getResult() != null
                && response.getResult().getAccountId() != null) {

            saveTransferHistory(
                    response.getResult().getAccountId(), // 상대방 계좌 ID
                    sendAccountNumber, // 출금자 계좌번호
                    dto.getSendName(), // 송금자 이름 정보
                    TransferType.DEPOSIT,
                    dto.getAmount(),
                    CurrencyCode.KRW,
                    TransferMethod.GENERAL
            );
        }
    }
}
