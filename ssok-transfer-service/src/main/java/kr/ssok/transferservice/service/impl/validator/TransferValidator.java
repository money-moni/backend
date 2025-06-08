package kr.ssok.transferservice.service.impl.validator;

import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransferValidator {

    /**
     * 송금 금액 검증 메서드
     *
     * @param amount 송금 금액
     * @throws TransferException 금액이 유효하지 않을 경우
     */
    public void validateTransferAmount(Long amount) {
        if (amount == null || amount <= 0) {
            log.warn("유효하지 않은 송금 금액: {}", amount);
            throw new TransferException(TransferResponseStatus.INVALID_TRANSFER_AMOUNT);
        }
    }

    /**
     * 출금 계좌와 입금 계좌가 동일한지 검증하는 메서드
     *
     * @param sendAccountNumber 출금 계좌 번호
     * @param recvAccountNumber 입금 계좌 번호
     * @throws TransferException 계좌가 동일한 경우 예외 발생
     */
    public void validateSameAccount(String sendAccountNumber, String recvAccountNumber) {
        if (sendAccountNumber != null && sendAccountNumber.equals(recvAccountNumber)) {
            log.warn("출금 계좌와 입금 계좌가 동일합니다: {}", sendAccountNumber);
            throw new TransferException(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED);
        }
    }
}