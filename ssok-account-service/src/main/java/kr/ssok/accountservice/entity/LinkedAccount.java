package kr.ssok.accountservice.entity;

import jakarta.persistence.*;
import kr.ssok.accountservice.entity.enums.AccountTypeCode;
import kr.ssok.accountservice.entity.enums.BankCode;
import kr.ssok.common.entity.TimeStamp;
import lombok.*;

/**
 * 사용자가 연동한 계좌 정보를 저장하는 JPA 엔티티
 *
 * <p>계좌 번호, 은행 코드, 계좌 별칭, 주계좌 여부, 계좌 타입, 사용자 ID 등을 관리합니다.</p>
 */
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class LinkedAccount extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", nullable = false, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank_code", nullable = false, updatable = false)
    private BankCode bankCode;

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

    /**
     * 계좌의 별칭(alias)을 업데이트합니다.
     *
     * @param accountAlias 새로 설정할 계좌 별칭
     */
    public void updateAlias(String accountAlias) {
        this.accountAlias = accountAlias;
    }

    /**
     * 주계좌(primary account) 여부를 업데이트합니다.
     *
     * @param isPrimaryAccount 주계좌로 설정할지 여부 (true 또는 false)
     */
    public void updatePrimaryAccount(Boolean isPrimaryAccount) {
        this.isPrimaryAccount = isPrimaryAccount;
    }
}
