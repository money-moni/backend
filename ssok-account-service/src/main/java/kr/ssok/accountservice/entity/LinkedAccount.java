package kr.ssok.accountservice.entity;

import jakarta.persistence.*;
import kr.ssok.accountservice.dto.response.AccountBalanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자가 연동한 계좌 정보를 저장하는 JPA 엔티티
 *
 * <p>계좌 번호, 은행 코드, 계좌 별칭, 주계좌 여부, 계좌 타입, 사용자 ID 등을 관리합니다.</p>
 *
 * <p>주로 연동 계좌 생성 및 연동 계좌 조회 기능에서 사용되며,
 * {@link AccountResponseDto}, {@link AccountBalanceResponseDto}로 변환하는 메소드를 제공합니다.</p>
 *
 * <p>생성일자, 수정일자는 스프링 데이터 JPA의 Auditing 기능을 통해 자동 관리됩니다.</p>
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LinkedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", nullable = false, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_code", nullable = false, updatable = false)
    private BankCode bankCode;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "is_primary_account", nullable = false)
    private Boolean isPrimaryAccount = false;   // 디퐅트는 false로 지정

    @Column(name = "account_alias")
    private String accountAlias;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false, updatable = false)
    private AccountTypeCode accountTypeCode;
}
