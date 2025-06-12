package kr.ssok.accountservice.exception;

import kr.ssok.accountservice.exception.grpc.GrpcExceptionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GrpcExceptionHandlingAspect {
    @Around("execution(* kr.ssok.accountservice.grpc.client..*(..))")
    public Object grpcExceptionHandlingAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (io.grpc.StatusRuntimeException e) {
            throw GrpcExceptionUtil.fromStatusRuntimeException(e);
        }
    }
}
