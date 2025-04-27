package kr.ssok.accountservice.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum BankCode {
    SSOK_BANK(1, "ssokbank"),
    KAKAO_BANK(2, "kakaobank"),
    KOOKMIN_BANK(3, "kbbank"),
    SHINHAN_BANK(4, "shinhanbank"),
    WOORI_BANK(5, "wooribank"),
    HANA_BANK(6, "kebbank"),
    NH_BANK(7, "nhbank"),
    IBK_BANK(8, "ibkbank"),
    K_BANK(9, "kbank"),
    TOSS_BANK(10, "tossbank");

    private final int idx;
    private final String value;

    private static final Map<Integer, BankCode> IDX_MAP = new HashMap<>();

    static {
        for (BankCode bankCode : BankCode.values()) {
            IDX_MAP.put(bankCode.getIdx(), bankCode);
        }
    }

    public static BankCode fromIdx(Long idx) {
        BankCode bankCode = IDX_MAP.get(idx.intValue());
        if (bankCode == null) {
            throw new IllegalArgumentException("Invalid BankCode idx: " + idx);
        }
        return bankCode;
    }
}
