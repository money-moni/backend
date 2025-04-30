package kr.ssok.accountservice.dto.response;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 계좌 기본 정보에 잔액 정보를 추가로 포함하는 응답 DTO
 *
 * <p>주로 연동 계좌 전체 조회 및 상세 조회 API 응답에 사용되며,
 * 계좌의 기본 정보(계좌 ID, 계좌 번호, 은행 코드 및 이름, 별칭, 주계좌 여부, 계좌 타입 코드)와 함께 잔액 정보를 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@SuperBuilder
@Getter
public class AccountBalanceResponseDto extends AccountResponseDto {
    private Long balance;

    /**
     * LinkedAccount 엔티티를 AccountBalanceResponseDto로 변환합니다.
     *
     * @param balance 계좌 잔액
     * @param account 연동 계좌 엔티티
     * @return AccountBalanceResponseDto 객체
     */
    public static AccountBalanceResponseDto from(Long balance, LinkedAccount account) {
        return AccountBalanceResponseDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .bankCode(account.getBankCode().getIdx())
                .bankName(account.getBankCode().getValue())
                .accountAlias(account.getAccountAlias())
                .isPrimaryAccount(account.getIsPrimaryAccount())
                .accountTypeCode(account.getAccountTypeCode().getValue())
                .balance(balance)
                .build();
    }
}
