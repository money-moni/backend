package kr.ssok.notificationservice.service;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 알리고 SMS 서비스
 * 알리고 SMS API를 통해 인증코드를 발송하는 서비스를 제공합니다.
 */
@Service
@NoArgsConstructor
public class AligoService {

    private static final String SMS_API_URL = "https://apis.aligo.in/send/";

    @Value("${aligo.API_KEY}")
    private String API_KEY;

    @Value("${aligo.USER_ID}")
    private String USER_ID;

    @Value("${aligo.SENDER}")
    private String SENDER;

    /**
     * 인증번호 문자 발송
     * 알리고 SMS API를 통해 인증번호가 포함된 SMS를 발송합니다.
     * 현재는 테스트 모드(testmode_yn=Y)로 설정되어 있습니다.
     * 
     * @param phoneNumber 인증코드를 받을 휴대폰 번호
     * @param verificationCode 발송할 인증코드
     */
    public void sendVerificationCode(String phoneNumber, String verificationCode) {
        phoneNumber = formatPhoneNumber(phoneNumber); // 전화번호 변환
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("key", API_KEY);
        map.add("user_id", USER_ID);
        map.add("sender", SENDER);
        map.add("receiver", phoneNumber);
        map.add("msg", "쏙! 인증번호를 안내해드립니다. " + verificationCode);
        map.add("testmode_yn", "Y");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(SMS_API_URL, HttpMethod.POST, request, String.class);
            System.out.println("Response Status: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 전화번호 변환 메서드
     * 국제 표준 전화번호 형식(+82)을 국내 전화번호 형식(0)으로 변환합니다.
     * 
     * @param phoneNumber 변환할 전화번호
     * @return 변환된 전화번호
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.startsWith("+82")) {
            // "+82"를 "0"으로 변환하고 나머지 문자열은 그대로 유지
            return "0" + phoneNumber.substring(3);
        }
        // 기존 형태 유지
        return phoneNumber;
    }
}

