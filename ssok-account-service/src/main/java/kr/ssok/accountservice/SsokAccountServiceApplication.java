package kr.ssok.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class SsokAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokAccountServiceApplication.class, args);
    }

}
