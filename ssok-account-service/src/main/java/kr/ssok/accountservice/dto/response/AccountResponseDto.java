package kr.ssok.accountservice.dto.response;

import kr.ssok.accountservice.entity.LinkedAccount;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * 계좌 정보를 클라이언트에 응답하기 위한 DTO
 *
 * <p>주로 연동 계좌 생성, 삭제, 별명 수정, 주 계좌 변경 API 응답에 사용되며,
 * 계좌의 기본 정보(계좌 ID, 계좌 번호, 은행 코드 및 이름, 별칭, 주계좌 여부, 계좌 타입 코드)를 포함합니다.</p>
 *
 * <p>이 객체는 읽기 전용으로 사용되며, 빌더 패턴을 통해 생성됩니다.</p>
 */
@SuperBuilder
@Getter
public class AccountResponseDto {
    private Long accountId;
    private String accountNumber;
    private int bankCode;
    private String bankName;
    private String accountAlias;
    private boolean isPrimaryAccount;
    private String accountTypeCode;


    /**
     * LinkedAccount 엔티티를 AccountResponseDto로 변환합니다.
     *
     * @return AccountResponseDto 객체
     */
    public static AccountResponseDto from(LinkedAccount account) {
        return AccountResponseDto.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .bankCode(account.getBankCode().getIdx())
                .bankName(account.getBankCode().getValue())
                .accountAlias(account.getAccountAlias())
                .isPrimaryAccount(account.getIsPrimaryAccount())
                .accountTypeCode(account.getAccountTypeCode().getValue())
                .build();
    }
}
