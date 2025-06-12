package kr.ssok.bluetoothservice.exception;

import kr.ssok.bluetoothservice.exception.grpc.GrpcExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class GrpcExceptionHandlingAspect {
    @Around("execution(* kr.ssok.bluetoothservice.grpc.client..*(..))")
    public Object grpcExceptionHandlingAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (io.grpc.StatusRuntimeException e) {
            log.error("gRPC Status: {}, Description: {}, Trailers: {}", e.getStatus(), e.getStatus().getDescription(), e.getTrailers());
            throw GrpcExceptionUtil.fromStatusRuntimeException(e);
        }
    }
}
