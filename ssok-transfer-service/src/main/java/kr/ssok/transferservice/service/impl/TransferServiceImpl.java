package kr.ssok.transferservice.service.impl;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.OpenBankingClient;
import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.response.AccountResponseDto;
import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.dto.response.OpenBankingResponse;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;
import kr.ssok.transferservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferBluetoothRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.entity.TransferHistory;
import kr.ssok.transferservice.entity.enums.CurrencyCode;
import kr.ssok.transferservice.entity.enums.TransferMethod;
import kr.ssok.transferservice.entity.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.kafka.message.KafkaNotificationMessageDto;
import kr.ssok.transferservice.kafka.producer.NotificationProducer;
import kr.ssok.transferservice.repository.TransferHistoryRepository;
import kr.ssok.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${external.openbanking-service.api-key}")
    private String OPENBANKING_API_KEY;

    // Kafka 알림 프로듀서
    private final NotificationProducer notificationProducer;

    private void sendKafkaNotification(Long userId, String senderName, Integer bankCode, Long amount, TransferType type) {
        KafkaNotificationMessageDto kafkaMessage = KafkaNotificationMessageDto.builder()
                .userId(userId)
                .senderName(senderName)
                .bankCode(bankCode)
                .amount(amount)
                .transferType(type)
                .build();

        notificationProducer.send(kafkaMessage);
    }

    /**
     * 일반 송금을 처리하는 메서드
     *
     * @param userId 사용자 ID
     * @param dto 송금 요청 DTO
     * @param transferMethod 송금 방법 (일반/블루투스 등)
     * @return 송금 응답 DTO
     */
    @Transactional
    @Override
    public TransferResponseDto transfer(Long userId, TransferRequestDto dto, TransferMethod transferMethod) {
        // 0. 송금 금액이 0보다 큰지 검증
        validateTransferAmount(dto.getAmount());

        long start = System.currentTimeMillis();
        // 1. 계좌 서비스에서 출금 계좌번호 조회
        String sendAccountNumber = findSendAccountNumber(dto.getSendAccountId(), userId);
        long end = System.currentTimeMillis();
        log.info("[SSOK-ACCOUNT] 출금 계좌번호 조회 요청 시간: {}ms", end - start);

        // 2. 츨금/입금 계좌 동일 여부 검증
        validateSameAccount(sendAccountNumber, dto.getRecvAccountNumber());

        start = System.currentTimeMillis();
        // 3. 오픈뱅킹 송금 요청
        requestOpenBankingTransfer(sendAccountNumber, dto);
        end = System.currentTimeMillis();
        log.info("[OPEN-BANKING] 오픈뱅킹 송금 요청 시간: {}ms", end - start);

        start = System.currentTimeMillis();
        // 4. 출금 내역 저장
        saveTransferHistory(dto.getSendAccountId(), dto.getRecvAccountNumber(), dto.getRecvName(),
                TransferType.WITHDRAWAL, dto.getAmount(), CurrencyCode.KRW, transferMethod);
        end = System.currentTimeMillis();
        log.info("[DB] 출금 내역 저장 시간: {}ms", end - start);

        // 5. 상대방 계좌번호로 계좌 ID 조회 후, 입금 이력 추가 저장 (SSOK 유저인 경우만)
        saveDepositHistoryIfReceiverExists(sendAccountNumber, dto, transferMethod);

        return buildTransferResponse(dto);
    }

    /**
     * 블루투스 송금을 처리하는 메서드
     *
     * @param userId 사용자 ID
     * @param requestDto 블루투스 송금 요청 DTO
     * @param transferMethod 송금 방법 (BLUETOOTH)
     * @return 블루투스 송금 응답 DTO
     */
    @Transactional
    @Override
    public BluetoothTransferResponseDto bluetoothTransfer(Long userId, BluetoothTransferRequestDto requestDto, TransferMethod transferMethod) {
        // 0. 송금 금액이 0보다 큰지 검증
        validateTransferAmount(requestDto.getAmount());

        long start = System.currentTimeMillis();
        // 1. 상대방 계좌 정보 조회 및 송금 요청 DTO 생성
        TransferBluetoothRequestDto transferRequestDto = createTransferRequest(requestDto);
        long end = System.currentTimeMillis();
        log.info("[SSOK-ACCOUNT-BLUETOOTH] 출금 계좌 정보 조회 요청 시간: {}ms", end - start);

        start = System.currentTimeMillis();
        // 2. 계좌 서비스에서 출금 계좌번호 조회
        String sendAccountNumber = findSendAccountNumber(transferRequestDto.getSendAccountId(), userId);
        end = System.currentTimeMillis();
        log.info("[SSOK-ACCOUNT-BLUETOOTH] 출금 계좌번호 조회 요청 시간: {}ms", end - start);

        // 3. 츨금/입금 계좌 동일 여부 검증
        validateSameAccount(sendAccountNumber, transferRequestDto.getRecvAccountNumber());

        start = System.currentTimeMillis();
        // 4. 오픈뱅킹 송금 요청
        requestOpenBankingTransfer(sendAccountNumber, createTransferRequest(transferRequestDto));
        end = System.currentTimeMillis();
        log.info("[OPEN-BANKING-BLUETOOTH] 오픈뱅킹 송금 요청 시간: {}ms", end - start);

        start = System.currentTimeMillis();
        // 5. 출금 내역 저장 (마스킹 처리)
        saveTransferHistory(transferRequestDto.getSendAccountId(),
                maskAccountNumber(transferRequestDto.getRecvAccountNumber()), // 계좌 번호 마스킹
                maskUsername(transferRequestDto.getRecvName()),               // 상대방 이름 마스킹
                TransferType.WITHDRAWAL, transferRequestDto.getAmount(),
                CurrencyCode.KRW, transferMethod);
        end = System.currentTimeMillis();
        log.info("[DB-BLUETOOTH] 출금 내역 저장 시간: {}ms", end - start);

        start = System.currentTimeMillis();
        // 6. 입금 내역 저장 (블루투스 송금은 상대방도 SSOK 유저)
        saveTransferHistory(transferRequestDto.getRecvAccountId(),
                maskAccountNumber(sendAccountNumber),                         // 상대방 계좌 번호 마스킹
                maskUsername(transferRequestDto.getSendName()),               // 상대방 이름 마스킹
                TransferType.DEPOSIT, transferRequestDto.getAmount(),
                CurrencyCode.KRW, transferMethod);
        end = System.currentTimeMillis();
        log.info("[DB-BLUETOOTH] 입금 내역 저장 시간: {}ms", end - start);

        start = System.currentTimeMillis();
        sendKafkaNotification(
                requestDto.getRecvUserId(),               // 수신자 userId
                transferRequestDto.getSendName(),         // 송신자 이름
                transferRequestDto.getRecvBankCode(),     // 수신자 은행 코드
                requestDto.getAmount(),                   // 금액
                TransferType.DEPOSIT                      // 송금 유형 (입금)
        );
        end = System.currentTimeMillis();
        log.info("[SSOK-NOTIFICATION-BLUETOOTH] 카프카 푸시 알림 요청 시간: {}ms", end - start);


        return buildBluetoothResponse(transferRequestDto);
    }

    /**
     * 송금 금액 검증 메서드
     *
     * @param amount 송금 금액
     * @throws TransferException 금액이 유효하지 않을 경우
     */
    private void validateTransferAmount(Long amount) {
        if (amount == null || amount <= 0) {
            log.error("유효하지 않은 송금 금액: {}", amount);
            throw new TransferException(TransferResponseStatus.INVALID_TRANSFER_AMOUNT);
        }
    }

    /**
     * 출금 계좌 번호 조회 메서드
     *
     * @param accountId 계좌 ID
     * @param userId 사용자 ID
     * @return 출금 계좌 번호
     */
    private String findSendAccountNumber(Long accountId, Long userId) {
        BaseResponse<AccountResponseDto> response = this.accountServiceClient.getAccountInfo(accountId, userId.toString());

        if (!response.getIsSuccess()) {
            log.error("출금 계좌 조회 실패: {}", response.getMessage());
            throw new TransferException(TransferResponseStatus.ACCOUNT_LOOKUP_FAILED);
        }
        return response.getResult().getAccountNumber();
    }

    /**
     * 오픈뱅킹 송금 요청 메서드
     *
     * @param sendAccountNumber 출금 계좌 번호
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

        OpenBankingResponse response = this.openBankingClient.sendTransferRequest(OPENBANKING_API_KEY, request);

        if (!response.getIsSuccess()) {
            log.error("오픈뱅킹 송금 실패: {}", response.getMessage());
            throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
        }
    }

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
    private void saveTransferHistory(Long accountId, String counterpartAccount, String counterpartName,
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

    /**
     * 상대방 계좌 ID가 존재하면 입금 이력 저장
     *
     * @param sendAccountNumber 송금자 계좌번호
     * @param dto 송금 요청 DTO
     */
    private void saveDepositHistoryIfReceiverExists(String sendAccountNumber, TransferRequestDto dto, TransferMethod transferMethod) {
        long start = System.currentTimeMillis();
        BaseResponse<AccountIdResponseDto> response =
                this.accountServiceClient.getAccountId(dto.getRecvAccountNumber());
        long end = System.currentTimeMillis();
        log.info("[SSOK-ACCOUNT] 송금 수신자 유저 조회 시간: {}ms", end - start);

        if (response.getIsSuccess()
                && response.getCode() == 2200
                && response.getResult() != null
                && response.getResult().getAccountId() != null) {

            start = System.currentTimeMillis();
            saveTransferHistory(
                    response.getResult().getAccountId(), // 상대방 계좌 ID
                    sendAccountNumber,                   // 출금자 계좌번호
                    dto.getSendName(),                   // 송금자 이름 정보
                    TransferType.DEPOSIT,
                    dto.getAmount(),
                    CurrencyCode.KRW,
                    transferMethod
            );
            end = System.currentTimeMillis();
            log.info("[DB] 송금 수신자 송금 내역 저장 시간: {}ms", end - start);

            // 푸시 알림(kafka)
            start = System.currentTimeMillis();

            sendKafkaNotification(
                    response.getResult().getUserId(),               // 수신자 userId
                    dto.getSendName(),                              // 송신자 이름
                    dto.getRecvBankCode(),                          // 수신자 은행 코드
                    dto.getAmount(),                                // 금액
                    TransferType.DEPOSIT                            // 송금 유형 (입금)
            );
            end = System.currentTimeMillis();
            log.info("[SSOK-NOTIFICATION] 카프카 푸시 알림 요청 시간: {}ms", end - start);
        }
    }

    /**
     * 오픈뱅킹 송금 요청 DTO 생성
     *
     * @param dto 블루투스 송금 요청 DTO
     * @return 오픈 뱅킹 송금 요청 DTO
     */
    private TransferRequestDto createTransferRequest(TransferBluetoothRequestDto dto) {
        return TransferRequestDto.builder()
                .sendAccountId(dto.getSendAccountId())
                .sendBankCode(dto.getSendBankCode())
                .sendName(dto.getSendName())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .recvBankCode(dto.getRecvBankCode())
                .recvName(dto.getRecvName())
                .amount(dto.getAmount())
                .build();
    }

    /**
     * 블루투스 송금 요청 DTO 생성
     *
     * @param requestDto 블루투스 송금 요청 DTO
     * @return 송금 요청 DTO
     */
    private TransferBluetoothRequestDto createTransferRequest(BluetoothTransferRequestDto requestDto) {
        // 상대방 계좌 정보 조회
        BaseResponse<PrimaryAccountResponseDto> response = accountServiceClient.getAccountInfo(requestDto.getRecvUserId().toString());
        if (!response.getIsSuccess()) {
            throw new TransferException(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED);
        }
        PrimaryAccountResponseDto accountInfo = response.getResult();

        // 송금 요청 DTO 생성
        return TransferBluetoothRequestDto.builder()
                .sendAccountId(requestDto.getSendAccountId())
                .sendBankCode(requestDto.getSendBankCode())
                .sendName(requestDto.getSendName())
                .recvAccountId(accountInfo.getAccountId())
                .recvAccountNumber(accountInfo.getAccountNumber())  // 상대방 계좌번호
                .recvBankCode(accountInfo.getBankCode())            // 상대방 은행 코드
                .recvName(accountInfo.getUsername())                // 상대방 이름
                .amount(requestDto.getAmount())
                .build();
    }

    /**
     * 송금 응답 DTO 생성
     *
     * @param dto 송금 요청 DTO
     * @return 송금 응답 DTO
     */
    private TransferResponseDto buildTransferResponse(TransferRequestDto dto) {
        return TransferResponseDto.builder()
                .sendAccountId(dto.getSendAccountId())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .amount(dto.getAmount())
                .build();
    }

    /**
     * 블루투스 송금 응답 DTO 생성
     *
     * @param dto 송금 요청 DTO
     * @return 블루투스 송금 응답 DTO
     */
    private BluetoothTransferResponseDto buildBluetoothResponse(TransferBluetoothRequestDto dto) {
        return BluetoothTransferResponseDto.builder()
                .sendAccountId(dto.getSendAccountId())
                .recvName(maskUsername(dto.getRecvName()))
                .amount(dto.getAmount())
                .build();
    }

    /**
     * 계좌 번호 마스킹 처리
     * @param accountNumber 계좌 번호
     * @return 마스킹 처리된 계좌 번호
     */
    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return accountNumber.substring(0, accountNumber.length() - 4) + "****";
    }

    /**
     * 유저 이름 마스킹 처리 (두 번째 글자를 *로 변경)
     * @param username 원본 유저 이름
     * @return 마스킹 처리된 유저 이름
     */
    private String maskUsername(String username) {
        if (username == null || username.length() < 2) {
            return username;
        }
        return username.charAt(0) + "*" + username.substring(2);
    }

    /**
     * 출금 계좌와 입금 계좌가 동일한지 검증하는 메서드
     *
     * @param sendAccountNumber 출금 계좌 번호
     * @param recvAccountNumber 입금 계좌 번호
     * @throws TransferException 계좌가 동일한 경우 예외 발생
     */
    private void validateSameAccount(String sendAccountNumber, String recvAccountNumber) {
        if (sendAccountNumber != null && sendAccountNumber.equals(recvAccountNumber)) {
            log.warn("출금 계좌와 입금 계좌가 동일합니다: {}", sendAccountNumber);
            throw new TransferException(TransferResponseStatus.SAME_ACCOUNT_TRANSFER_NOT_ALLOWED);
        }
    }
}
