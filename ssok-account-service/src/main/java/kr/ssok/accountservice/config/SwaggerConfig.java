package kr.ssok.accountservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger(OpenAPI) 문서 생성을 위한 설정 클래스
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {

        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("계좌 서비스")
                .description("SSOK-ACCOUNT-SERVICE REST API")
                .version("1.0.0");
    }
}
