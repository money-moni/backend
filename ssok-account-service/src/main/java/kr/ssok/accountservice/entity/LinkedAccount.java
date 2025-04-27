package kr.ssok.accountservice.entity;

import jakarta.persistence.*;
import kr.ssok.accountservice.dto.response.AccountBlanceResponseDto;
import kr.ssok.accountservice.dto.response.AccountResponseDto;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@NoArgsConstructor
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

    @Column(name = "is_primary_account", nullable = false)
    private Boolean isPrimaryAccount = false;   // 디퐅트는 false로 지정

    @Column(name = "account_alias")
    private String accountAlias;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false, updatable = false)
    private AccountTypeCode accountTypeCode;

    public AccountResponseDto toAccountResponseDto() {
        return AccountResponseDto.builder()
                .accountId(this.accountId)
                .accountNumber(this.accountNumber)
                .bankCode(this.bankCode.getIdx())
                .bankName(this.bankCode.getValue())
                .accountAlias(this.accountAlias)
                .isPrimaryAccount(this.isPrimaryAccount)
                .accountTypeCode(this.accountTypeCode)
                .build();
    }

    public AccountBlanceResponseDto toAccountBlanceResponseDto(Long balance) {
        return AccountBlanceResponseDto.builder()
                .accountId(this.accountId)
                .accountNumber(this.accountNumber)
                .bankCode(this.bankCode.getIdx())
                .bankName(this.bankCode.getValue())
                .accountAlias(this.accountAlias)
                .isPrimaryAccount(this.isPrimaryAccount)
                .accountTypeCode(this.accountTypeCode)
                .balance(balance)
                .build();
    }


}
