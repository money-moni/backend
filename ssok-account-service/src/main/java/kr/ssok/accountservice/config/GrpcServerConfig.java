package kr.ssok.accountservice.config;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import kr.ssok.accountservice.grpc.server.AccountGrpcInternalServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class GrpcServerConfig {
    private final AccountGrpcInternalServiceImpl accountGrpcInternalService;

    @Value("${grpc.account-server.port}")
    private int GRPC_ACCOUNT_SERVER_PORT;

    private Server server;
    private ExecutorService executor;

    @PostConstruct
    public void startServer() throws IOException {

        // TODO. 스레드 풀 갯수는 최적화가 필요할 듯
        executor = Executors.newFixedThreadPool(4);
        server = Grpc.newServerBuilderForPort(GRPC_ACCOUNT_SERVER_PORT, InsecureServerCredentials.create())
                .executor(executor)
                .addService(this.accountGrpcInternalService) // 직접 서비스 주입
                .build()
                .start();
        System.out.println("gRPC server started on port 50051");
    }

    @PreDestroy
    public void stopServer() throws InterruptedException {
        if (server != null) {
            // graceful 하게 종료
            // 종료까지 최대 30초 기다림
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
        if (executor != null) {
            // 스레드 풀 정리
            executor.shutdown();
        }
        System.out.println("gRPC server stopped");
    }
}
