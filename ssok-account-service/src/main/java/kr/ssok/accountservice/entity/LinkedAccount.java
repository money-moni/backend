package kr.ssok.accountservice.entity;

import jakarta.persistence.*;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "bank_code", nullable = false)
    private Long bankCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "is_primary_account", nullable = false)
    private Boolean isPrimaryAccount;

    @Column(name = "account_alias")
    private String accountAlias;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type_code", nullable = false)
    private AccountTypeCode accountTypeCode;

}
