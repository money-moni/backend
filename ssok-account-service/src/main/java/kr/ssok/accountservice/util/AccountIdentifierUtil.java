package kr.ssok.accountservice.util;

/**
 * 계좌 관련 Redis 키 생성을 위한 유틸리티 클래스
 *
 * <p>이 클래스는 Redis에서 계좌 정보를 조회하거나 저장할 때 사용할
 * key 및 value 문자열을 생성하는 정적 메서드를 제공합니다.</p>
 *
 * <ul>
 *     <li><b>Key</b>: 사용자 기준 Redis key (예: {@code account:lookup:123})</li>
 *     <li><b>Value</b>: 계좌 고유 식별 문자열 (예: {@code 1:123-456-7890:2})</li>
 * </ul>
 *
 * <p>Redis 구조 예시:
 * <pre>
 *     key = "account:lookup:1"
 *     value = Set {
 *         "1:1234567890:2",
 *         "4:9876543210:1"
 *     }
 * </pre>
 * </p>
 */
public class AccountIdentifierUtil {

    /**
     * 주어진 사용자 ID를 기반으로 Redis에 저장될 계좌 조회용 키(key)를 생성합니다.
     *
     * <p>생성되는 키의 형식은 다음과 같습니다:
     * <pre>
     *     account:lookup:[userId]
     *     예) account:lookup:123
     * </pre>
     *
     * @param userId 사용자 ID
     * @return Redis에서 계좌 목록을 저장하거나 조회할 때 사용할 키 문자열
     */
    public static String buildLookupKey(Long userId) {
        return "account:lookup:" + userId;
    }
    /**
     * Redis의 Set 값으로 사용할 계좌 식별 문자열을 생성합니다.
     * <p>
     * 생성되는 식별자의 형식은 다음과 같습니다:
     * <pre>
     *     [은행 코드]:[계좌 번호]:[계좌 타입 코드]
     *     예) 1:1234567890:2
     * </pre>
     *
     * @param bankCode 은행 코드 (예: 1 = SSOK_BANK)
     * @param accountNumber 계좌 번호 (예: "123-456-7890")
     * @param accountTypeCode 계좌 타입 코드 (예: 2 = 적금)
     * @return Redis Set에 저장할 계좌 식별 문자열
     */
    public static String buildLookupValue(int bankCode, String accountNumber, int accountTypeCode) {
        return bankCode + ":" +
                accountNumber + ":" +
                accountTypeCode;
    }
}
