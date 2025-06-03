package kr.ssok.transferservice.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 지원하는 은행 목록을 나타내는 Enum
 *
 * 각 은행은 고유한 idx(식별자)와 value(은행 코드 문자열)
 * 푸시 알림의 은행명 시별에 사용
 */
@Getter
@AllArgsConstructor
public enum BankCode {
    SSOK_BANK(1, "SSOK뱅크"),
    KAKAO_BANK(2, "카카오뱅크"),
    KOOKMIN_BANK(3, "KB국민은행"),
    SHINHAN_BANK(4, "신한은행"),
    WOORI_BANK(5, "우리은행"),
    HANA_BANK(6, "KEB하나은행"),
    NH_BANK(7, "NH농협은행"),
    IBK_BANK(8, "IBK기업은행"),
    K_BANK(9, "케이뱅크"),
    TOSS_BANK(10, "토스뱅크");

    private final int idx;
    private final String value;

    private static final Map<Integer, BankCode> IDX_MAP = new HashMap<>();

    static {
        for (BankCode bankCode : BankCode.values()) {
            IDX_MAP.put(bankCode.getIdx(), bankCode);
        }
    }

    public static BankCode fromIdx(int idx) {
        BankCode bankCode = IDX_MAP.get(idx);
        if (bankCode == null) {
            throw new IllegalArgumentException("Invalid BankCode idx: " + idx);
        }
        return bankCode;
    }
}
