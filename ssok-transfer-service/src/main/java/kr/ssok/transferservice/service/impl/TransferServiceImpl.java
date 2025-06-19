package kr.ssok.transferservice.service.impl;

import kr.ssok.common.exception.BaseResponse;
import kr.ssok.transferservice.client.AccountServiceClient;
import kr.ssok.transferservice.client.webclient.OpenBankingApiClient;
import kr.ssok.transferservice.client.dto.response.AccountIdResponseDto;
import kr.ssok.transferservice.client.dto.request.OpenBankingTransferRequestDto;
import kr.ssok.transferservice.client.dto.response.OpenBankingResponse;
import kr.ssok.transferservice.client.dto.response.PrimaryAccountResponseDto;
import kr.ssok.transferservice.dto.request.BluetoothTransferRequestDto;
import kr.ssok.transferservice.dto.request.TransferBluetoothRequestDto;
import kr.ssok.transferservice.dto.request.TransferRequestDto;
import kr.ssok.transferservice.dto.response.BluetoothTransferResponseDto;
import kr.ssok.transferservice.dto.response.TransferResponseDto;
import kr.ssok.transferservice.enums.BankCode;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import kr.ssok.transferservice.exception.TransferException;
import kr.ssok.transferservice.exception.TransferResponseStatus;
import kr.ssok.transferservice.grpc.client.AccountService;
import kr.ssok.transferservice.service.TransferService;
import kr.ssok.transferservice.service.impl.helper.AccountInfoResolver;
import kr.ssok.transferservice.service.impl.helper.TransferHistoryRecorder;
import kr.ssok.transferservice.service.impl.helper.TransferNotificationSender;
import kr.ssok.transferservice.service.impl.validator.TransferValidator;
import kr.ssok.transferservice.util.MaskingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * 송금 서비스의 실제 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferValidator validator;
    private final AccountInfoResolver accountResolver;
    private final TransferHistoryRecorder transferHistoryRecorder;
    private final TransferNotificationSender notificationSender;

    private final OpenBankingApiClient openBankingWebClient;
    private final AccountService accountServiceClient;

    @Value("${external.openbanking-service.api-key}")
    private String OPENBANKING_API_KEY;

    /**
     * 일반 송금을 처리하는 메서드
     * - 오픈뱅킹 호출은 WebClient 논블로킹 I/O + CompletableFuture 비동기 실행으로 처리
     *
     * @Async("customExecutorWebClient") 로 지정된 스레드풀에서 실행
     * @param userId 사용자 ID
     * @param dto 송금 요청 DTO
     * @param transferMethod 송금 방법 (일반/블루투스 등)
     * @return 송금 응답 DTO
     */
    @Async("customExecutorWebClient")
    @Transactional
    @Override
    public CompletableFuture<TransferResponseDto> transfer(Long userId, TransferRequestDto dto, TransferMethod transferMethod) {
        // 0. 송금 금액이 0보다 큰지 검증
        validator.validateTransferAmount(dto.getAmount());

        // 1. 계좌 서비스에서 출금 계좌번호 조회
        String sendAccountNumber = accountResolver.findSendAccountNumber(dto.getSendAccountId(), userId);

        // 2. 츨금/입금 계좌 동일 여부 검증
        validator.validateSameAccount(sendAccountNumber, dto.getRecvAccountNumber());

        // 3. OpenBankingClient를 통해 비동기 송금 요청
        OpenBankingTransferRequestDto obReq = OpenBankingTransferRequestDto.builder()
                .sendAccountNumber(sendAccountNumber)
                .sendBankCode(dto.getSendBankCode())
                .sendName(dto.getSendName())
                .recvAccountNumber(dto.getRecvAccountNumber())
                .recvBankCode(dto.getRecvBankCode())
                .recvName(dto.getRecvName())
                .amount(dto.getAmount())
                .build();

        // 4. WebClient 비동기 호출 및 후속 처리
        return openBankingWebClient
                .sendTransferRequestAsync(obReq)    // CompletableFuture<OpenBankingResponse>
                .thenApply(response -> {            // 응답 완료 시점에 이 블록이 실행
                    if (!response.isSuccess()) {
                        log.error("오픈뱅킹 송금 실패: {}", response.getMessage());
                        throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
                    }
                    // 3) 출금/입금 내역 저장
                    transferHistoryRecorder.saveTransferHistory(
                            dto.getSendAccountId(), dto.getRecvAccountNumber(), dto.getRecvName(), BankCode.fromIdx(dto.getRecvBankCode()), TransferType.WITHDRAWAL,
                            dto.getAmount(), CurrencyCode.KRW, transferMethod);
                    saveDepositHistoryIfReceiverExists(sendAccountNumber, dto, transferMethod);

                    // 4) 결과 DTO 생성
                    return buildTransferResponse(dto);
                });
    }

    /**
     * 블루투스 송금을 처리하는 메서드
     *
     * @param userId 사용자 ID
     * @param requestDto 블루투스 송금 요청 DTO
     * @param transferMethod 송금 방법 (BLUETOOTH)
     * @return 블루투스 송금 응답 DTO
     */
    @Async("customExecutorWebClient")
    @Transactional
    @Override
    public CompletableFuture<BluetoothTransferResponseDto> bluetoothTransfer(Long userId, BluetoothTransferRequestDto requestDto, TransferMethod transferMethod) {
        // 0. 송금 금액이 0보다 큰지 검증
        validator.validateTransferAmount(requestDto.getAmount());

        // 1. 상대방 계좌 정보 조회 및 송금 요청 DTO 생성
        TransferBluetoothRequestDto transferRequestDto = createTransferRequest(requestDto);

        // 2. 계좌 서비스에서 출금 계좌번호 조회
        String sendAccountNumber = accountResolver.findSendAccountNumber(transferRequestDto.getSendAccountId(), userId);

        // 3. 츨금/입금 계좌 동일 여부 검증
        validator.validateSameAccount(sendAccountNumber, transferRequestDto.getRecvAccountNumber());

        // 4. OpenBanking 비동기 송금 요청 DTO 빌드
        OpenBankingTransferRequestDto obReq = OpenBankingTransferRequestDto.builder()
                .sendAccountNumber(sendAccountNumber)
                .sendBankCode(transferRequestDto.getSendBankCode())
                .sendName(transferRequestDto.getSendName())
                .recvAccountNumber(transferRequestDto.getRecvAccountNumber())
                .recvBankCode(transferRequestDto.getRecvBankCode())
                .recvName(transferRequestDto.getRecvName())
                .amount(transferRequestDto.getAmount())
                .build();

        // 5. WebClient 비동기 호출 및 후속 처리
        return openBankingWebClient
                .sendTransferRequestAsync(obReq)
                .thenApply(response -> {
                    if (!response.isSuccess()) {
                        log.error("오픈뱅킹 블루투스 송금 실패: {}", response.getMessage());
                        throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
                    }
                    // 5. 출금 이력 저장 (마스킹)
                    transferHistoryRecorder.saveTransferHistory(
                            transferRequestDto.getSendAccountId(),
                            MaskingUtils.maskAccountNumber(transferRequestDto.getRecvAccountNumber()), MaskingUtils.maskUsername(transferRequestDto.getRecvName()),
                            BankCode.fromIdx(transferRequestDto.getRecvBankCode()), TransferType.WITHDRAWAL, transferRequestDto.getAmount(), CurrencyCode.KRW, transferMethod);
                    // 6. 입금 이력 저장 (마스킹)
                    transferHistoryRecorder.saveTransferHistory(
                            transferRequestDto.getRecvAccountId(),
                            MaskingUtils.maskAccountNumber(sendAccountNumber), MaskingUtils.maskUsername(transferRequestDto.getSendName()),
                            BankCode.fromIdx(transferRequestDto.getSendBankCode()), TransferType.DEPOSIT, transferRequestDto.getAmount(), CurrencyCode.KRW, transferMethod);
                    // 7. 푸시 알림
                    notificationSender.sendKafkaNotification(
                            requestDto.getRecvUserId(), transferRequestDto.getRecvAccountId(), transferRequestDto.getSendName(), transferRequestDto.getRecvBankCode(), requestDto.getAmount(), TransferType.DEPOSIT);
                    // 8. 결과 DTO 반환
                    return buildBluetoothResponse(transferRequestDto);
                });
    }

//    /**
//     * 오픈뱅킹 송금 요청 메서드
//     *
//     * @param sendAccountNumber 출금 계좌 번호
//     * @param dto 송금 요청 DTO
//     */
//    private void requestOpenBankingTransfer(String sendAccountNumber, TransferRequestDto dto) {
//        OpenBankingTransferRequestDto request = OpenBankingTransferRequestDto.builder()
//                .sendAccountNumber(sendAccountNumber)
//                .sendBankCode(dto.getSendBankCode())
//                .sendName(dto.getSendName())
//                .recvAccountNumber(dto.getRecvAccountNumber())
//                .recvBankCode(dto.getRecvBankCode())
//                .recvName(dto.getRecvName())
//                .amount(dto.getAmount())
//                .build();
//
//        OpenBankingResponse response = this.openBankingClient.sendTransferRequest(OPENBANKING_API_KEY, request);
//
//        if (!response.getIsSuccess()) {
//            log.error("오픈뱅킹 송금 실패: {}", response.getMessage());
//            throw new TransferException(TransferResponseStatus.REMITTANCE_FAILED);
//        }
//    }

    /**
     * 상대방 계좌 ID가 존재하면 입금 이력 저장
     *
     * @param sendAccountNumber 송금자 계좌번호
     * @param dto 송금 요청 DTO
     */
    private void saveDepositHistoryIfReceiverExists(String sendAccountNumber, TransferRequestDto dto, TransferMethod transferMethod) {

//        BaseResponse<AccountIdResponseDto> response =
//                this.accountServiceClient.getAccountId(dto.getRecvAccountNumber());
        AccountIdResponseDto response =
                this.accountServiceClient.getAccountId(dto.getRecvAccountNumber());

        // NPE 방지용
        if (response == null || response.getAccountId() == null) {
            log.info("[SSOK-ACCOUNT] 상대방 계좌 ID가 없어 입금 이력 저장을 건너뜁니다. recvAccountNumber={}",
                    dto.getRecvAccountNumber());
            return;
        }

//        if (response.getIsSuccess()
//                && response.getCode() == 2200
//                && response.getResult() != null
//                && response.getResult().getAccountId() != null) {

            transferHistoryRecorder.saveTransferHistory(
                    response.getAccountId(), // 상대방 계좌 ID
                    sendAccountNumber,                   // 출금자 계좌번호
                    dto.getSendName(),                   // 송금자 이름 정보
                    BankCode.fromIdx(dto.getSendBankCode()),
                    TransferType.DEPOSIT,
                    dto.getAmount(),
                    CurrencyCode.KRW,
                    transferMethod
            );

            // 푸시 알림(kafka)
            notificationSender.sendKafkaNotification(
                    response.getUserId(),               // 수신자 userId
                    response.getAccountId(),            // 수신자 계좌 ID
                    dto.getSendName(),                  // 송신자 이름
                    dto.getRecvBankCode(),              // 수신자 은행 코드
                    dto.getAmount(),                    // 금액
                    TransferType.DEPOSIT                // 송금 유형 (입금)
            );
//        }
    }

//    /**
//     * 오픈뱅킹 송금 요청 DTO 생성
//     *
//     * @param dto 블루투스 송금 요청 DTO
//     * @return 오픈 뱅킹 송금 요청 DTO
//     */
//    private TransferRequestDto createTransferRequest(TransferBluetoothRequestDto dto) {
//        return TransferRequestDto.builder()
//                .sendAccountId(dto.getSendAccountId())
//                .sendBankCode(dto.getSendBankCode())
//                .sendName(dto.getSendName())
//                .recvAccountNumber(dto.getRecvAccountNumber())
//                .recvBankCode(dto.getRecvBankCode())
//                .recvName(dto.getRecvName())
//                .amount(dto.getAmount())
//                .build();
//    }

    /**
     * 블루투스 송금 요청 DTO 생성
     *
     * @param requestDto 블루투스 송금 요청 DTO
     * @return 송금 요청 DTO
     */
    private TransferBluetoothRequestDto createTransferRequest(BluetoothTransferRequestDto requestDto) {
        // 상대방 계좌 정보 조회
//        BaseResponse<PrimaryAccountResponseDto> response = accountServiceClient.getAccountInfo(requestDto.getRecvUserId().toString());
//        if (!response.getIsSuccess()) {
//            throw new TransferException(TransferResponseStatus.COUNTERPART_ACCOUNT_LOOKUP_FAILED);
//        }
//        PrimaryAccountResponseDto accountInfo = response.getResult();
        PrimaryAccountResponseDto accountInfo = accountServiceClient.getPrimaryAccountInfo(requestDto.getRecvUserId().toString());

        // 테스트용 log
        log.info("[DEBUG] 받은 PrimaryAccountResponseDto accountInfo: accountId={}", accountInfo.getAccountId());

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
                .recvName(MaskingUtils.maskUsername(dto.getRecvName()))
                .amount(dto.getAmount())
                .build();
    }
}
