package kr.ssok.transferservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SsokTransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokTransferServiceApplication.class, args);
    }
}
