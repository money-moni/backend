package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.enums.BankCode;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.grpc.client.AccountService;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.exception.TransferResponseStatus;

import kr.ssok.transferservice.util.MaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferHistoryRecorder {

    private final TransferHistoryRepository transferHistoryRepository;

    private final AccountService accountServiceClient;
    private final TransferNotificationSender notifSender;

    /**
     * 송금 이력 저장 메서드
     *
     * @param accountId 계좌 ID
     * @param counterpartAccount 상대방 계좌 번호
     * @param counterpartName 상대방 이름
     * @param counterpartBankCode 상대방 계좌 은행 코드
     * @param transferType 송금 유형 (출금/입금)
     * @param amount 송금 금액
     * @param currencyCode 통화 코드
     * @param transferMethod 송금 방법
     */
    public void saveTransferHistory(Long accountId, String counterpartAccount, String counterpartName, BankCode counterpartBankCode,
                                         TransferType transferType, Long amount, CurrencyCode currencyCode, TransferMethod transferMethod) {
        TransferHistory history = TransferHistory.builder()
                .accountId(accountId)
                .counterpartAccount(counterpartAccount)
                .counterpartName(counterpartName)
                .counterpartBankCode(counterpartBankCode)
                .transferType(transferType)
                .transferMoney(amount)
                .currencyCode(currencyCode)
                .transferMethod(transferMethod)
                .build();

        try {
            transferHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("송금 이력 저장 실패: accountId={}, amount={}, error={}",
                    accountId, amount, e.getMessage());
            throw new TransferException(TransferResponseStatus.TRANSFER_HISTORY_SAVE_ERROR);
        }
    }

    /**
     * 일반 송금: NO masking. 출금·입금 포함, 실패 시 전체 롤백
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordGeneralTransfer(
            Long sendAccountId,
            String sendAccountNumber,
            String sendName,
            int sendBankCode,
            String recvAccountNumber,
            String recvName,
            int recvBankCode,
            Long amount,
            CurrencyCode currencyCode,
            TransferMethod method) {

        // 출금
        transferHistoryRepository.save(TransferHistory.builder()
                .accountId(sendAccountId)
                .counterpartAccount(recvAccountNumber)
                .counterpartName(recvName)
                .counterpartBankCode(BankCode.fromIdx(recvBankCode))
                .transferType(TransferType.WITHDRAWAL)
                .transferMoney(amount)
                .currencyCode(currencyCode)
                .transferMethod(method)
                .build());

        // 입금 조회
        AccountIdResponseDto response = accountServiceClient.getAccountId(recvAccountNumber);
        if (response == null || response.getAccountId() == null) {
            log.info("[SSOK-ACCOUNT] 상대방 계좌 ID가 없어 입금 이력 저장을 건너뜁니다. recvAccountNumber={}",
                    recvAccountNumber);
            return;
            //throw new TransferException(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED);
        }

        // 입금
        transferHistoryRepository.save(TransferHistory.builder()
                .accountId(response.getAccountId())
                .counterpartAccount(sendAccountNumber)
                .counterpartName(sendName)
                .counterpartBankCode(BankCode.fromIdx(sendBankCode))
                .transferType(TransferType.DEPOSIT)
                .transferMoney(amount)
                .currencyCode(currencyCode)
                .transferMethod(method)
                .build());

        // 알림
        notifSender.sendKafkaNotification(
                response.getUserId(), response.getAccountId(), sendName, recvBankCode, amount, TransferType.DEPOSIT);
    }

    /**
     * 블루투스 송금: masking 적용. 출금·입금·알림까지 모두 롤백
     */
    @Transactional(rollbackFor = Exception.class)
    public void recordBluetoothTransfer(
            Long sendAccountId,
            String sendAccountNumber,
            String sendName,
            int sendBankCode,
            Long recvUserId,
            PrimaryAccountResponseDto accountInfo,
            Long amount,
            CurrencyCode currencyCode,
            TransferMethod method) {

        // 출금 (masking account)
        transferHistoryRepository.save(TransferHistory.builder()
                .accountId(sendAccountId)
                .counterpartAccount(MaskingUtils.maskAccountNumber(accountInfo.getAccountNumber()))
                .counterpartName(MaskingUtils.maskUsername(accountInfo.getUsername()))
                .counterpartBankCode(BankCode.fromIdx(accountInfo.getBankCode()))
                .transferType(TransferType.WITHDRAWAL)
                .transferMoney(amount)
                .currencyCode(currencyCode)
                .transferMethod(method)
                .build());

        // 입금
        transferHistoryRepository.save(TransferHistory.builder()
                .accountId(accountInfo.getAccountId())
                .counterpartAccount(MaskingUtils.maskAccountNumber(sendAccountNumber))
                .counterpartName(MaskingUtils.maskUsername(sendName))
                .counterpartBankCode(BankCode.fromIdx(sendBankCode))
                .transferType(TransferType.DEPOSIT)
                .transferMoney(amount)
                .currencyCode(currencyCode)
                .transferMethod(method)
                .build());

        // 알림
        notifSender.sendKafkaNotification(
                recvUserId, accountInfo.getAccountId(), sendName, accountInfo.getBankCode(), amount, TransferType.DEPOSIT);
    }
}