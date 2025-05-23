package kr.ssok.accountservice.config;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import jakarta.annotation.PreDestroy;
import kr.ssok.common.grpc.user.UserServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {
    @Value("${grpc.user-server.address}")
    private String GRPC_USER_SERVER_ADDRESS;


    @Value("${grpc.user-server.port}")
    private int GRPC_USER_SERVER_PORT;

    private ManagedChannel managedChannel;

    @Bean
    public ManagedChannel managedChannel() {
        String target = GRPC_USER_SERVER_ADDRESS + ":" + GRPC_USER_SERVER_PORT;
        this.managedChannel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
                .build();
        return this.managedChannel;
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(ManagedChannel managedChannel) {
        return UserServiceGrpc.newBlockingStub(managedChannel);
    }

    @PreDestroy
    public void shutdown() {
        if (this.managedChannel != null && !this.managedChannel.isShutdown()) {
            this.managedChannel.shutdown();
        }
    }
}
