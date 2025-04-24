package kr.ssok.transferservice;

import kr.ssok.common.util.TestUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SsokTransferServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokTransferServiceApplication.class, args);

        // ssok-common 의존성을 가져옴
        TestUtil.Test();
    }
}
