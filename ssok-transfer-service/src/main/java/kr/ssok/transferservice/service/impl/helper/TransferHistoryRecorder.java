package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.exception.TransferResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferHistoryRecorder {

    private final TransferHistoryRepository transferHistoryRepository;

    /**
     * 송금 이력 저장 메서드
     *
     * @param accountId 계좌 ID
     * @param counterpartAccount 상대방 계좌 번호
     * @param counterpartName 상대방 이름
     * @param transferType 송금 유형 (출금/입금)
     * @param amount 송금 금액
     * @param currencyCode 통화 코드
     * @param transferMethod 송금 방법
     */
    public void saveTransferHistory(Long accountId, String counterpartAccount, String counterpartName,
                                         TransferType transferType, Long amount, CurrencyCode currencyCode, TransferMethod transferMethod) {
        TransferHistory history = TransferHistory.builder()
                .accountId(accountId)
                .counterpartAccount(counterpartAccount)
                .counterpartName(counterpartName)
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
}