package kr.ssok.example;

import kr.ssok.common.util.TestUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SsokExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsokExampleApplication.class, args);

        // ssok-common 의존성을 가져옴
        TestUtil.Test();
    }
}
