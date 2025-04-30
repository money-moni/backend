package kr.ssok.accountservice.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 계좌의 종류를 나타내는 Enum
 *
 * <p>각 계좌 타입은 고유한 idx(식별자)와 value(계좌 유형 이름)를 가집니다.</p>
 *
 * <p>주로 계좌 등록, 조회 시 계좌 타입을 구분하는 데 사용됩니다.</p>
 */
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

    public static AccountTypeCode fromIdx(int idx) {
        AccountTypeCode code = IDX_MAP.get(idx);
        if (code == null) {
            throw new IllegalArgumentException("Invalid AccountTypeCode idx: " + idx);
        }
        return code;
    }
}

