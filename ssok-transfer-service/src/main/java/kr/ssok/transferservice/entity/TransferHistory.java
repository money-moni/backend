package kr.ssok.transferservice.entity;

import jakarta.persistence.*;
import kr.ssok.transferservice.enums.CurrencyCode;
import kr.ssok.transferservice.enums.TransferMethod;
import kr.ssok.transferservice.enums.TransferType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 송금 이력을 저장하기 위한 JPA 엔티티.
 * DB 테이블: transfer_history
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 생성자 제한
@AllArgsConstructor // @Builder 사용을 위해 필요
public class TransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // PK, 자동 증가

    @Column(nullable = false)
    private Long accountId;                 // 본인 계좌 ID

    @Column(nullable = false)
    private String counterpartAccount;      // 상대방 계좌 번호

    @Column(nullable = false)
    private String counterpartName;         // 상대방 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType transferType;      // 입금 / 출금

    @Column(nullable = false)
    private Long transferMoney;             // 송금 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currencyCode;      // 통화 코드 (원화 / 달러)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferMethod transferMethod;  // 송금 방식 (일반 / 클루투스)

    @Column(nullable = false)
    private LocalDateTime createdAt;        // 생성 시각

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
