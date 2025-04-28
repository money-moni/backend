package kr.ssok.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SsokUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokUserServiceApplication.class, args);
    }

}
