package kr.ssok.userservice.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * 외부 시스템 연동 테스트를 위한 Mock 구성
 * 필요한 경우 실제 API 대신 Mock 서버를 사용할 수 있습니다.
 */
@TestConfiguration
@Profile("mock-external")
public class MockExternalSystemConfig {

    /**
     * 외부 API 호출용 RestTemplate 빈
     * Mock 서버로 라우팅하도록 구성할 수 있습니다.
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    @Primary
    public RestTemplate mockRestTemplate() {
        return new RestTemplate();
    }
    
    /**
     * 실제 Mock 서버 구성이 필요한 경우 WireMock 또는 MockServer와 같은
     * 라이브러리를 사용하여 아래와 같이 구성
     * 
     * Example with WireMock:
     * 
     * @Bean(destroyMethod = "stop")
     * public WireMockServer wireMockServer() {
     *   WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
     *   wireMockServer.start();
     *
     *   // Bank API 모킹
     *   wireMockServer.stubFor(post(urlEqualTo("/api/bank/account"))
     *       .willReturn(aResponse()
     *           .withStatus(200)
     *           .withHeader("Content-Type", "application/json")
     *           .withBody("{"accountNumber":"123-456-789"}")
     *       ));
     *
     *   // Aligo API 모킹
     *   wireMockServer.stubFor(post(urlEqualTo("/api/aligo/verify"))
     *       .willReturn(aResponse()
     *           .withStatus(200)
     *       ));
     *
     *   return wireMockServer;
     * }
     */
}
