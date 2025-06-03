package kr.ssok.transferservice.util;

import org.springframework.stereotype.Component;

public class MaskingUtils {

    /**
     * 계좌 번호 마스킹 처리
     * @param accountNumber 계좌 번호
     * @return 마스킹 처리된 계좌 번호
     */
    public static String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return accountNumber.substring(0, accountNumber.length() - 4) + "****";
    }

    /**
     * 유저 이름 마스킹 처리 (두 번째 글자를 *로 변경)
     * @param username 원본 유저 이름
     * @return 마스킹 처리된 유저 이름
     */
    public static String maskUsername(String username) {
        if (username == null || username.length() < 2) {
            return username;
        }
        return username.charAt(0) + "*" + username.substring(2);
    }
}