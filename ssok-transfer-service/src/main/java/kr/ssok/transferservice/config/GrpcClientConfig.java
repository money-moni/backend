package kr.ssok.transferservice.config;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;

import kr.ssok.common.grpc.account.AccountServiceGrpc;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.account-server.address}")
    private String GRPC_ACCOUNT_SERVER_ADDRESS;

    @Value("${grpc.account-server.port}")
    private int GRPC_ACCOUNT_SERVER_PORT;

    private ManagedChannel managedChannel;

    @Bean
    public ManagedChannel managedChannel() {
        String target = GRPC_ACCOUNT_SERVER_ADDRESS + ":" + GRPC_ACCOUNT_SERVER_PORT;
        this.managedChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        return this.managedChannel;
    }

    @Bean
    public AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub(ManagedChannel managedChannel) {
        return AccountServiceGrpc.newBlockingStub(managedChannel);
    }

    @PreDestroy
    public void shutdown() {
        if (this.managedChannel != null && !this.managedChannel.isShutdown()) {
            this.managedChannel.shutdown();
        }
    }
}
