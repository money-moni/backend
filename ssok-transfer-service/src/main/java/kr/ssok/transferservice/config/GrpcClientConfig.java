package kr.ssok.transferservice.config;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

import kr.ssok.common.grpc.account.AccountServiceGrpc;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.account-server.address}")
    private String GRPC_ACCOUNT_SERVER_ADDRESS;

    @Value("${grpc.account-server.port}")
    private int GRPC_ACCOUNT_SERVER_PORT;

    private ManagedChannel managedChannel;

    @Bean
    public ManagedChannel managedChannel() {
        String target = "dns:///" + GRPC_ACCOUNT_SERVER_ADDRESS + ":" + GRPC_ACCOUNT_SERVER_PORT;
        this.managedChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .defaultServiceConfig(Map.of(
                        "loadBalancingPolicy", "round_robin",
                        "retryPolicy", Map.of(
                                "maxAttempts", 3,
                                "initialBackoff", "0.5s",
                                "maxBackoff", "10s",
                                "retryableStatusCodes", List.of("UNAVAILABLE")
                        )
                ))
                .keepAliveTime(30, TimeUnit.SECONDS)
                .build();
        return managedChannel;
    }

    @Bean
    public AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub(ManagedChannel managedChannel) {
        return AccountServiceGrpc.newBlockingStub(managedChannel);
    }

    @PreDestroy
    public void shutdown() {
        shutdownChannel(this.managedChannel);
    }

    private void shutdownChannel(ManagedChannel channel) {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
            try {
                if (!channel.awaitTermination(5, TimeUnit.SECONDS)) {
                    channel.shutdownNow();
                }
            } catch (InterruptedException e) {
                channel.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
