package kr.ssok.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SsokAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokAccountServiceApplication.class, args);
    }

}
