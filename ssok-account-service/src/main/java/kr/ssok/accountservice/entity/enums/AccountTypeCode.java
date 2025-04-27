package kr.ssok.accountservice.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum AccountTypeCode {
    DEPOSIT(1, "예금"),
    SAVINGS(2, "적금"),
    SUBSCRIPTION(3, "청약");

    private final int idx;
    private final String value;

    private static final Map<Integer, AccountTypeCode> IDX_MAP = new HashMap<>();

    static {
        for (AccountTypeCode code : AccountTypeCode.values()) {
            IDX_MAP.put(code.getIdx(), code);
        }
    }

    public static AccountTypeCode fromIdx(Long idx) {
        AccountTypeCode code = IDX_MAP.get(idx.intValue());
        if (code == null) {
            throw new IllegalArgumentException("Invalid AccountTypeCode idx: " + idx);
        }
        return code;
    }
}

