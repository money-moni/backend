package kr.ssok.bluetoothservice.config;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import jakarta.annotation.PreDestroy;
import kr.ssok.common.grpc.account.AccountServiceGrpc;
import kr.ssok.common.grpc.user.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.account-server.address}")
    private String GRPC_ACCOUNT_SERVER_ADDRESS;
    @Value("${grpc.account-server.port}")
    private int GRPC_ACCOUNT_SERVER_PORT;

    @Value("${grpc.user-server.address}")
    private String GRPC_USER_SERVER_ADDRESS;
    @Value("${grpc.user-server.port}")
    private int GRPC_USER_SERVER_PORT;

    private ManagedChannel accountChannel;
    private ManagedChannel userChannel;

    @Bean(name = "accountChannel")
    public ManagedChannel accountChannel() {
        String target = "dns:///" + GRPC_ACCOUNT_SERVER_ADDRESS + ":" + GRPC_ACCOUNT_SERVER_PORT;
        this.accountChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
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
        return accountChannel;
    }

    @Bean(name = "userChannel")
    public ManagedChannel userChannel() {
        String target = "dns:///" + GRPC_USER_SERVER_ADDRESS + ":" + GRPC_USER_SERVER_PORT;
        this.userChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
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
        return userChannel;
    }

    @Bean
    public AccountServiceGrpc.AccountServiceBlockingStub accountStub(
            @Qualifier("accountChannel") ManagedChannel accountChannel) {
        return AccountServiceGrpc.newBlockingStub(accountChannel);
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userStub(
            @Qualifier("userChannel") ManagedChannel userChannel) {
        return UserServiceGrpc.newBlockingStub(userChannel);
    }

    @PreDestroy
    public void shutdown() {
        shutdownChannel(accountChannel);
        shutdownChannel(userChannel);
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
