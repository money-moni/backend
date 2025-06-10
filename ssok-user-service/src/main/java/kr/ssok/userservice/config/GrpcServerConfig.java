package kr.ssok.userservice.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import kr.ssok.userservice.grpc.server.UserGrpcInternalServiceImpl;
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
    private final UserGrpcInternalServiceImpl userGrpcInternalService;

    @Value("${grpc.account-server.port}")
    private int GRPC_USER_SERVER_PORT;

    private Server server;
    private ExecutorService executor;

    @PostConstruct
    public void startServer() throws IOException {
        int cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores * 2);
        server = ServerBuilder.forPort(GRPC_USER_SERVER_PORT)
                .addService(this.userGrpcInternalService)
                .executor(executor)
                .maxConnectionAge(30, TimeUnit.SECONDS)  // 30초마다 연결 종료
                .build()
                .start();
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
    }
}
