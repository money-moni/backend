package kr.ssok.transferservice.service.impl.helper;

import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
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

        transferHistoryRepository.save(history);
    }
}