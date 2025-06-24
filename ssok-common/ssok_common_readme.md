# SSOK Common Library

> 모든 마이크로서비스에서 공통으로 사용하는 핵심 라이브러리

## 📋 개요

SSOK Common Library는 SSOK 플랫폼의 **모든 마이크로서비스에서 공통으로 사용하는 핵심 기능들을 제공하는 라이브러리**입니다. 일관된 예외 처리, 응답 형식, AOP 로깅, gRPC 통신 인터페이스, JPA 공통 엔티티 등을 포함하여 개발 생산성과 코드 일관성을 높입니다.

### 주요 기능

- **공통 예외 처리**: 일관된 예외 처리 및 응답 형식 제공
- **AOP 로깅**: 컨트롤러, 서비스, 성능 로깅 자동화
- **gRPC 인터페이스**: 서비스 간 통신을 위한 Protocol Buffers 정의
- **JPA 공통 엔티티**: 생성/수정 시간 자동 관리
- **로깅 유틸리티**: 민감 정보 마스킹, Trace ID 관리

## 🏗️ 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                    SSOK Common Library                     │
├─────────────────────────────────────────────────────────────┤
│  Exception Handling  │  AOP Logging  │  gRPC Interfaces   │
│  • BaseException     │  • @Controller │  • User Service    │
│  • BaseResponse      │  • @Service    │  • Account Service │
│  • ResponseStatus    │  • @Performance│  • Proto Buffers   │
├─────────────────────────────────────────────────────────────┤
│  JPA Entities       │  Logging Utils │  Trace Management  │
│  • TimeStamp        │  • LoggingUtil │  • TraceIdGenerator│
│  • Auditing         │  • Masking     │  • MDC Management  │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   User Service  │  │ Account Service │  │ Transfer Service│
│                 │  │                 │  │                 │
│ • 예외 처리     │  │ • 예외 처리     │  │ • 예외 처리     │
│ • AOP 로깅      │  │ • AOP 로깅      │  │ • AOP 로깅      │
│ • gRPC 서버     │  │ • gRPC 서버     │  │ • gRPC 클라이언트│
│ • TimeStamp     │  │ • TimeStamp     │  │ • TimeStamp     │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

## 🔧 기술 스택

| 분류 | 기술 |
|------|------|
| **Core** | Spring Core, Spring Context |
| **Data** | Spring Data JPA, Hibernate |
| **AOP** | AspectJ, Spring AOP |
| **gRPC** | gRPC Java, Protocol Buffers |
| **Serialization** | Jackson, JSON Processing |
| **Annotations** | Jakarta Annotations |
| **Build** | Gradle, Protobuf Plugin |

## 📁 프로젝트 구조

```
ssok-common/
├── src/main/java/kr/ssok/common/
│   ├── entity/                     # JPA 공통 엔티티
│   │   └── TimeStamp.java          # 생성/수정 시간 관리
│   ├── exception/                  # 예외 처리
│   │   ├── BaseException.java      # 기본 예외 클래스
│   │   ├── BaseResponse.java       # 공통 응답 형식
│   │   ├── ResponseStatus.java     # 응답 상태 인터페이스
│   │   ├── CommonResponseStatus.java # 공통 응답 상태
│   │   └── ExceptionHandler.java   # 전역 예외 핸들러
│   └── logging/                    # 로깅 관련
│       ├── annotation/             # 로깅 어노테이션
│       │   ├── ControllerLogging.java
│       │   ├── ServiceLogging.java
│       │   └── PerformanceLogging.java
│       ├── aspect/                 # AOP Aspect
│       │   ├── ControllerLoggingAspect.java
│       │   ├── ServiceLoggingAspect.java
│       │   └── PerformanceLoggingAspect.java
│       └── util/                   # 로깅 유틸리티
│           ├── LoggingUtil.java
│           └── TraceIdGenerator.java
├── src/main/proto/                 # gRPC Protocol Buffers
│   ├── user_service.proto          # User Service gRPC 정의
│   └── account_service.proto       # Account Service gRPC 정의
├── build.gradle                   # 빌드 설정
└── src/generated/                 # 생성된 gRPC 코드 (빌드 시 자동 생성)
```

## 🚨 예외 처리 시스템

### BaseException 및 ResponseStatus

모든 도메인별 예외는 `BaseException`을 상속받아 구현합니다.

```java
// 공통 예외 클래스
public class BaseException extends RuntimeException {
    private ResponseStatus status;
    
    public BaseException(ResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }
}

// 응답 상태 인터페이스
public interface ResponseStatus {
    boolean isSuccess();
    int getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
```

### 사용 예시

```java
// 도메인별 예외 상태 정의
@Getter
public enum UserResponseStatus implements ResponseStatus {
    LOGIN_SUCCESS(true, 2001, "로그인에 성공하였습니다."),
    INVALID_PIN_CODE(false, 4000, "유효하지 않은 PIN 번호입니다."),
    USER_NOT_FOUND(false, 5011, "사용자를 찾을 수 없습니다.");
    
    // 구현...
}

// 도메인별 예외 클래스
public class UserException extends BaseException {
    public UserException(UserResponseStatus status) {
        super(status);
    }
}

// 서비스에서 예외 발생
if (user == null) {
    throw new UserException(UserResponseStatus.USER_NOT_FOUND);
}
```

### BaseResponse 표준 응답 형식

```java
// 표준 응답 형식
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> {
    private Boolean isSuccess;
    private int code;
    private String message;
    private T result;
}

// 사용 예시
return ResponseEntity.ok(
    new BaseResponse<>(UserResponseStatus.LOGIN_SUCCESS, loginData)
);
```

## 📊 AOP 로깅 시스템

### @ControllerLogging

컨트롤러 메서드의 요청/응답을 자동으로 로깅합니다.

```java
@ControllerLogging(
    logParameters = true,
    logResult = true,
    logExecutionTime = true,
    maskSensitiveData = true
)
@PostMapping("/login")
public ResponseEntity<BaseResponse<LoginResponseDto>> login(
    @RequestBody LoginRequestDto requestDto) {
    // 구현...
}
```

**로그 출력 예시:**
```
[SSOK-A1B2C3D4][CONTROLLER-START] AuthController.login - Parameters: {"userId":123,"pinCode":"****"}
[SSOK-A1B2C3D4][CONTROLLER-SUCCESS] AuthController.login - Duration: 245ms
```

### @ServiceLogging

서비스 메서드의 실행을 자동으로 로깅합니다.

```java
@ServiceLogging(
    value = "사용자 로그인 처리",
    logParameters = true,
    logExecutionTime = true,
    logException = true
)
public LoginResponseDto login(LoginRequestDto requestDto) {
    // 구현...
}
```

### @PerformanceLogging

성능 측정 및 임계값 기반 경고 로깅을 제공합니다.

```java
@PerformanceLogging(
    warningThresholdMs = 1000,  // 1초 초과 시 경고
    errorThresholdMs = 5000,    // 5초 초과 시 오류
    alwaysLog = false
)
public List<Account> findAllAccounts(Long userId) {
    // 구현...
}
```

## 📡 gRPC 인터페이스 정의

### User Service gRPC

```protobuf
syntax = "proto3";

package user;

service UserService {
  rpc GetUserInfo(UserIdRequest) returns (UserInfoResponse);
}

message UserIdRequest {
  string user_id = 1;
}

message UserInfoResponse {
  string username = 1;
  string phoneNumber = 2;
  string profileImage = 3;
}
```

### Account Service gRPC

```protobuf
syntax = "proto3";

package account;

service AccountService {
  rpc GetAccountInfo(AccountInfoRequest) returns (AccountInfoResponse);
  rpc GetAccountIdByAccountNumber(AccountNumberRequest) returns (AccountIdResponse);
  rpc GetAccountIdsByUserId(UserIdRequest) returns (AccountIdsResponse);
  rpc GetPrimaryAccountInfo(UserIdRequest) returns (PrimaryAccountInfoResponse);
  rpc GetPrimaryAccountBalance(UserIdRequest) returns (PrimaryAccountBalanceResponse);
}

message AccountInfoRequest {
  int64 account_id = 1;
  string user_id = 2;
}

message AccountInfoResponse {
  int64 account_id = 1;
  int64 user_id = 2;
  string account_number = 3;
}
```

### 생성된 gRPC 코드 사용

```java
// gRPC 서버 구현
@Component
public class UserGrpcServiceImpl extends UserServiceGrpc.UserServiceImplBase {
    @Override
    public void getUserInfo(UserIdRequest request, StreamObserver<UserInfoResponse> responseObserver) {
        // 구현...
    }
}

// gRPC 클라이언트 사용
@Component
public class UserServiceClient {
    @Autowired
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    
    public UserInfoResponse getUserInfo(String userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
            .setUserId(userId)
            .build();
        return userServiceStub.getUserInfo(request);
    }
}
```

## 🗄️ JPA 공통 엔티티

### TimeStamp 기본 엔티티

```java
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class TimeStamp {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
```

### 사용 예시

```java
@Entity
public class User extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String phoneNumber;
    
    // createdAt, updatedAt은 자동으로 관리됨
}
```

## 🔍 로깅 유틸리티

### 민감 정보 마스킹

```java
public class LoggingUtil {
    // 민감한 정보 자동 마스킹
    public static String maskSensitiveData(String input) {
        String masked = input;
        masked = PIN_PATTERN.matcher(masked).replaceAll("\"pinCode\":\"****\"");
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("\"password\":\"****\"");
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("\"$1Token\":\"****\"");
        return masked;
    }
    
    // JSON 변환 및 마스킹
    public static String toMaskedJsonString(Object obj) {
        String jsonString = toJsonString(obj);
        return maskSensitiveData(jsonString);
    }
}
```

### Trace ID 관리

```java
public class TraceIdGenerator {
    public static String generate() {
        return "SSOK-" + UUID.randomUUID().toString()
            .replace("-", "").substring(0, 16).toUpperCase();
    }
}

// MDC를 통한 Trace ID 관리
LoggingUtil.setTraceId(TraceIdGenerator.generate());
String currentTraceId = LoggingUtil.getTraceId();
```

## 🚀 빌드 및 사용

### 의존성 추가

다른 서비스에서 ssok-common을 사용하려면 build.gradle에 의존성을 추가합니다.

```gradle
dependencies {
    implementation project(':ssok-common')
    
    // 다른 의존성들...
}
```

### gRPC 코드 생성

```bash
# Protocol Buffers 컴파일 및 gRPC 코드 생성
./gradlew :ssok-common:generateProto

# 또는 전체 빌드 시 자동 생성
./gradlew :ssok-common:build
```

### AOP 활성화

각 서비스에서 AOP 로깅을 사용하려면 설정을 추가합니다.

```java
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "kr.ssok.common.logging.aspect")
public class AopConfig {
    // AOP 설정
}
```

### JPA Auditing 활성화

```java
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    // JPA Auditing 설정
}
```

## 📊 로깅 구조

### 표준 로그 형식

```json
{
  "time": "2024-01-01T12:00:00.000+09:00",
  "level": "INFO",
  "logger": "kr.ssok.userservice.controller.AuthController",
  "thread": "http-nio-8080-exec-1",
  "message": "[SSOK-A1B2C3D4][CONTROLLER-START] AuthController.login - Parameters: {\"userId\":123,\"pinCode\":\"****\"}",
  "app": "ssok-user-service",
  "traceId": "SSOK-A1B2C3D4",
  "userId": "123"
}
```

### MDC 컨텍스트

```java
// MDC에 컨텍스트 정보 설정
LoggingUtil.setTraceId("SSOK-A1B2C3D4");
LoggingUtil.setUserId("123");

// 로그에 자동으로 포함됨
log.info("사용자 로그인 처리 시작");
// 출력: [SSOK-A1B2C3D4][사용자:123] 사용자 로그인 처리 시작
```

## 🧪 테스트

### 단위 테스트 실행

```bash
./gradlew :ssok-common:test
```

### 테스트 예시

```java
@ExtendWith(MockitoExtension.class)
class LoggingUtilTest {
    
    @Test
    void testMaskSensitiveData() {
        String input = "{\"pinCode\":1234,\"password\":\"secret\"}";
        String masked = LoggingUtil.maskSensitiveData(input);
        
        assertThat(masked).contains("\"pinCode\":\"****\"");
        assertThat(masked).contains("\"password\":\"****\"");
    }
    
    @Test
    void testTraceIdGeneration() {
        String traceId = TraceIdGenerator.generate();
        
        assertThat(traceId).startsWith("SSOK-");
        assertThat(TraceIdGenerator.isValid(traceId)).isTrue();
    }
}
```

## 📋 사용 가이드

### 1. 새로운 예외 상태 추가

```java
// 1. ResponseStatus 구현
@Getter
public enum TransferResponseStatus implements ResponseStatus {
    TRANSFER_SUCCESS(true, 3001, "송금이 완료되었습니다."),
    INSUFFICIENT_BALANCE(false, 4301, "잔액이 부족합니다.");
    
    // 구현...
}

// 2. 도메인 예외 클래스 생성
public class TransferException extends BaseException {
    public TransferException(TransferResponseStatus status) {
        super(status);
    }
}

// 3. 서비스에서 사용
if (balance < transferAmount) {
    throw new TransferException(TransferResponseStatus.INSUFFICIENT_BALANCE);
}
```

### 2. 새로운 gRPC 서비스 추가

```protobuf
// 1. proto 파일 생성 (src/main/proto/new_service.proto)
syntax = "proto3";

package newservice;

option java_package = "kr.ssok.common.grpc.newservice";

service NewService {
  rpc DoSomething(DoSomethingRequest) returns (DoSomethingResponse);
}

message DoSomethingRequest {
  string input = 1;
}

message DoSomethingResponse {
  string output = 1;
}
```

```bash
# 2. gRPC 코드 생성
./gradlew :ssok-common:generateProto
```

### 3. AOP 로깅 사용

```java
@RestController
@ControllerLogging  // 클래스 레벨 적용
public class ExampleController {
    
    @PostMapping("/example")
    @ControllerLogging(  // 메서드 레벨 오버라이드
        logParameters = true,
        logResult = false,
        maskSensitiveData = true
    )
    public ResponseEntity<BaseResponse<String>> example(@RequestBody ExampleDto dto) {
        return ResponseEntity.ok(new BaseResponse<>(CommonResponseStatus.SUCCESS, "OK"));
    }
}
```

## 🔄 버전 관리

### 호환성 가이드

- **Major Version (1.x.x)**: Breaking Changes
- **Minor Version (x.1.x)**: 새로운 기능 추가 (하위 호환)
- **Patch Version (x.x.1)**: 버그 수정 (하위 호환)

### 업그레이드 가이드

새 버전으로 업그레이드 시 다음 사항을 확인하세요:

1. **gRPC 인터페이스 변경**: proto 파일 호환성 확인
2. **예외 처리 변경**: ResponseStatus enum 변경 사항 확인
3. **로깅 설정 변경**: 어노테이션 파라미터 변경 사항 확인

## 📋 TODO / 개선사항

- [ ] **분산 추적**: OpenTelemetry 지원 추가
- [ ] **메트릭 수집**: Micrometer 통합
- [ ] **설정 관리**: Configuration Properties 표준화
- [ ] **보안 강화**: 민감 정보 마스킹 패턴 확장
- [ ] **성능 최적화**: AOP 오버헤드 최소화
- [ ] **테스트 유틸리티**: 공통 테스트 도구 제공
- [ ] **문서 생성**: gRPC 서비스 문서 자동 생성
- [ ] **코드 생성**: 도메인별 Exception, ResponseStatus 템플릿

## 🤝 기여 가이드

1. **Interface 설계**: 새로운 공통 기능 추가 시 인터페이스 우선 설계
2. **하위 호환성**: 기존 기능 변경 시 하위 호환성 유지
3. **테스트 필수**: 모든 기능에 대한 단위 테스트 작성
4. **문서 업데이트**: 새로운 기능 추가 시 README 업데이트
5. **gRPC 정의**: Protocol Buffers 변경 시 모든 서비스 호환성 확인

## 📞 문의

Common 라이브러리 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

> **Note**: 이 라이브러리는 SSOK 플랫폼의 모든 마이크로서비스에서 사용하는 핵심 라이브러리입니다. 변경 시 모든 서비스에 영향을 미치므로 신중하게 검토해주세요. 각 서비스별 사용법은 해당 서비스의 README를 참조하세요.