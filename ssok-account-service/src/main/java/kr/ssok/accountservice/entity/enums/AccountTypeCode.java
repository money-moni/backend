package kr.ssok.accountservice.entity.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountTypeCode {
    DEPOSIT(1, "예금"),
    SAVINGS(2, "적금"),
    SUBSCRIPTION(3, "청약");

    private final int idx;
    private final String value;
}
