# SSOK Account Service

> 계좌 관리 및 OpenBanking API 연동을 담당하는 마이크로서비스

## 📋 개요

SSOK Account Service는 SSOK 플랫폼의 **계좌 관리 시스템**을 담당하는 핵심 서비스입니다. 사용자의 계좌 등록, 조회, 관리와 함께 외부 OpenBanking API와의 연동을 통해 실시간 계좌 정보를 제공합니다.

### 주요 기능

- **계좌 관리**: 계좌 등록, 조회, 삭제, 별칭 설정, 주계좌 지정
- **OpenBanking 연동**: 실시간 계좌 목록, 잔액 조회, 실명 확인
- **내부 서비스 연동**: gRPC 및 REST API를 통한 다른 서비스와의 계좌 정보 제공
- **비동기 처리**: WebClient를 활용한 고성능 비동기 API 호출
- **계좌 보안**: 본인 명의 계좌만 등록 가능한 실명 확인 시스템

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Gateway       │    │ Account Service  │    │ OpenBanking API │
│                 │    │                  │    │                 │
│ • 요청 라우팅   │◄──►│ • 계좌 관리      │◄──►│ • 계좌 목록     │
│ • 인증 확인     │    │ • 실명 확인      │    │ • 잔액 조회     │
│                 │    │ • 잔액 조회      │    │ • 실명 확인     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                │ gRPC/REST
                                ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │      Redis       │    │ Other Services  │
│                 │    │                  │    │                 │
│ • 연동 계좌     │    │ • 캐시           │    │ • User Service  │
│ • 은행 코드     │    │ • 세션 관리      │    │ • Transfer      │
│ • 계좌 타입     │    │                  │    │ • Bluetooth     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🔧 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.4.4, Spring Data JPA |
| **Database** | PostgreSQL (주 DB), Redis (캐시) |
| **Communication** | REST API, gRPC, WebClient |
| **External APIs** | OpenBanking API |
| **Async Processing** | CompletableFuture, WebFlux |
| **Documentation** | OpenAPI 3.0 (Swagger) |
| **Monitoring** | Micrometer, Actuator |
| **Build** | Gradle |

## 📁 프로젝트 구조

```
ssok-account-service/
├── src/main/java/kr/ssok/accountservice/
│   ├── client/                    # 외부 서비스 클라이언트
│   │   ├── OpenBankingClient.java # OpenBanking API 클라이언트
│   │   └── UserServiceClient.java # User Service 클라이언트
│   ├── config/                    # 설정 클래스
│   │   ├── AopConfig.java         # AOP 설정
│   │   ├── AsyncConfig.java       # 비동기 처리 설정
│   │   ├── GrpcClientConfig.java  # gRPC 클라이언트 설정
│   │   ├── GrpcServerConfig.java  # gRPC 서버 설정
│   │   ├── RedisConfig.java       # Redis 설정
│   │   ├── SwaggerConfig.java     # API 문서 설정
│   │   └── WebClientConfig.java   # WebClient 설정
│   ├── controller/                # REST API 컨트롤러
│   │   ├── AccountController.java           # 계좌 관리 API
│   │   ├── AccountInternalController.java   # 내부 서비스용 API
│   │   └── AccountOpenBankingController.java # OpenBanking API
│   ├── dto/                       # 데이터 전송 객체
│   │   ├── request/               # 요청 DTO
│   │   │   ├── openbanking/       # OpenBanking 요청 DTO
│   │   │   ├── CreateAccountRequestDto.java
│   │   │   ├── UpdateAliasRequestDto.java
│   │   │   └── AccountOwnerRequestDto.java
│   │   └── response/              # 응답 DTO
│   │       ├── openbanking/       # OpenBanking 응답 DTO
│   │       ├── transferservice/   # Transfer Service용 DTO
│   │       ├── bluetoothservice/  # Bluetooth Service용 DTO
│   │       └── userservice/       # User Service용 DTO
│   ├── entity/                    # JPA 엔티티
│   │   ├── LinkedAccount.java     # 연동 계좌 엔티티
│   │   └── enums/
│   │       ├── BankCode.java      # 은행 코드 Enum
│   │       └── AccountTypeCode.java # 계좌 타입 Enum
│   ├── exception/                 # 예외 처리
│   │   ├── AccountException.java
│   │   ├── AccountExceptionHandler.java
│   │   ├── AccountResponseStatus.java
│   │   ├── feign/                 # Feign 클라이언트 예외
│   │   └── grpc/                  # gRPC 예외
│   ├── grpc/                      # gRPC 구현
│   │   ├── client/                # gRPC 클라이언트
│   │   └── server/                # gRPC 서버
│   ├── repository/                # 데이터 접근 계층
│   │   └── AccountRepository.java
│   ├── service/                   # 비즈니스 로직
│   │   ├── AccountService.java             # 계좌 관리 서비스
│   │   ├── AccountInternalService.java     # 내부 연동 서비스
│   │   ├── AccountOpenBankingService.java  # OpenBanking 서비스
│   │   └── impl/                          # 서비스 구현체
│   └── util/                      # 유틸리티
│       └── AccountIdentifierUtil.java
├── src/main/resources/
│   └── logback-spring.xml         # 로깅 설정
├── build.gradle                  # 빌드 설정
└── Dockerfile                    # 컨테이너 이미지 빌드
```

## 🗄️ 데이터베이스 스키마

### LinkedAccount 테이블
```sql
CREATE TABLE linked_account (
    account_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    bank_code VARCHAR(20) NOT NULL,
    is_primary_account BOOLEAN DEFAULT false,
    account_alias VARCHAR(50),
    user_id BIGINT NOT NULL,
    account_type_code VARCHAR(20) NOT NULL,
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 인덱스
CREATE INDEX idx_linked_account_user_id ON linked_account(user_id);
CREATE INDEX idx_linked_account_number ON linked_account(account_number);
CREATE INDEX idx_linked_account_primary ON linked_account(user_id, is_primary_account);
```

### 지원하는 은행 코드
```java
public enum BankCode {
    SSOK_BANK(1, "SSOK뱅크"),
    KAKAO_BANK(2, "카카오뱅크"),
    KOOKMIN_BANK(3, "KB국민은행"),
    SHINHAN_BANK(4, "신한은행"),
    WOORI_BANK(5, "우리은행"),
    HANA_BANK(6, "KEB하나은행"),
    NH_BANK(7, "NH농협은행"),
    IBK_BANK(8, "IBK기업은행"),
    K_BANK(9, "케이뱅크"),
    TOSS_BANK(10, "토스뱅크");
}
```

### 지원하는 계좌 타입
```java
public enum AccountTypeCode {
    DEPOSIT(1, "예금"),
    SAVINGS(2, "적금"),
    SUBSCRIPTION(3, "청약");
}
```

## 🔌 API 엔드포인트

### 계좌 관리 (`/api/accounts`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 계좌 등록 | ✅ |
| GET | `/` | 전체 계좌 목록 조회 (잔액 포함) | ✅ |
| GET | `/{accountId}` | 특정 계좌 조회 | ✅ |
| DELETE | `/{accountId}` | 계좌 삭제 | ✅ |
| PATCH | `/{accountId}/alias` | 계좌 별칭 수정 | ✅ |
| PATCH | `/{accountId}/primary` | 주계좌 설정 | ✅ |

### 내부 서비스 연동 (`/api/accounts/internal`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/account-info` | 계좌 정보 조회 (내부용) | ✅ |
| GET | `/id` | 계좌번호로 계좌 ID 조회 | ❌ |
| GET | `/account-ids` | 사용자 전체 계좌 ID 목록 | ✅ |
| GET | `/primary-account-info` | 주계좌 정보 조회 | ✅ |
| GET | `/primary-account-balance` | 주계좌 잔액 조회 | ✅ |

### OpenBanking 연동 (`/api/accounts/openbank`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | OpenBanking 전체 계좌 조회 | ✅ |
| POST | `/verify-name` | 실명 확인 | ❌ |

## 💼 주요 비즈니스 로직

### 계좌 등록 프로세스

1. **실명 확인**: OpenBanking API를 통해 계좌 소유자 확인
2. **중복 검증**: 이미 등록된 계좌인지 확인
3. **User Service 연동**: 사용자 정보 조회 및 실명 대조
4. **계좌 저장**: 검증 완료 후 데이터베이스에 저장
5. **주계좌 설정**: 첫 번째 계좌는 자동으로 주계좌 설정

### 잔액 조회 플로우

```java
public CompletableFuture<List<AccountBalanceResponseDto>> findAllAccounts(Long userId) {
    // 1. 사용자의 모든 계좌 조회
    List<LinkedAccount> accounts = accountRepository.findByUserIdAndIsDeletedFalse(userId);
    
    // 2. 각 계좌별 비동기 잔액 조회
    List<CompletableFuture<AccountBalanceResponseDto>> futures = accounts.stream()
        .map(account -> fetchAccountBalance(account))
        .toList();
    
    // 3. 모든 비동기 작업 완료 대기
    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream()
            .map(CompletableFuture::join)
            .toList());
}
```

### 주계좌 관리

- **자동 지정**: 첫 번째 등록 계좌는 자동으로 주계좌 설정
- **변경 처리**: 새로운 주계좌 설정 시 기존 주계좌 해제
- **삭제 제한**: 주계좌는 삭제 불가 (다른 계좌를 주계좌로 변경 후 삭제 가능)

## 🔗 외부 서비스 연동

### OpenBanking API 연동

```java
@Component
public class OpenBankingClient {
    // 전체 계좌 목록 조회
    public CompletableFuture<OpenBankingResponse<List<OpenBankingAllAccountsResponseDto>>> 
        sendAllAccountsRequest(OpenBankingAllAccountsRequestDto dto) {
        return openBankingWebClient.post()
            .uri("/api/openbank/accounts/request")
            .header("X-API-KEY", openBankingApiKey)
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<>() {})
            .toFuture();
    }
    
    // 계좌 잔액 조회
    public CompletableFuture<OpenBankingResponse<OpenBankingAccountBalanceResponseDto>> 
        sendAccountBalanceRequest(OpenBankingAccountBalanceRequestDto dto) {
        // 비동기 잔액 조회 로직
    }
    
    // 실명 확인
    public CompletableFuture<OpenBankingResponse<OpenBankingAccountOwnerResponseDto>> 
        sendAccountOwnerRequest(OpenBankingAccountOwnerRequestDto dto) {
        // 실명 확인 로직
    }
}
```

### User Service 연동

```java
// Feign Client를 통한 사용자 정보 조회
@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/users/internal/{userId}")
    BaseResponse<UserInfoResponseDto> getUserInfo(@PathVariable Long userId);
}
```

## 🔍 gRPC 내부 통신

다른 마이크로서비스에서 계좌 정보가 필요할 때 gRPC를 통해 효율적으로 통신합니다.

### gRPC 서비스 정의
```protobuf
service AccountService {
    rpc GetAccountInfo(AccountInfoRequest) returns (AccountInfoResponse);
    rpc GetAccountIdByAccountNumber(AccountNumberRequest) returns (AccountIdResponse);
    rpc GetAccountIdsByUserId(UserIdRequest) returns (AccountIdsResponse);
    rpc GetPrimaryAccountInfo(UserIdRequest) returns (PrimaryAccountInfoResponse);
    rpc GetPrimaryAccountBalance(UserIdRequest) returns (PrimaryAccountBalanceResponse);
}

message AccountInfoRequest {
    string userId = 1;
    int64 accountId = 2;
}

message AccountInfoResponse {
    int64 accountId = 1;
    int64 userId = 2;
    string accountNumber = 3;
}
```

### 사용 예시 (Transfer Service에서)
```java
@Component
public class AccountServiceClient {
    @Autowired
    private AccountServiceGrpc.AccountServiceBlockingStub accountServiceStub;
    
    public AccountInfoResponse getAccountInfo(String userId, Long accountId) {
        AccountInfoRequest request = AccountInfoRequest.newBuilder()
            .setUserId(userId)
            .setAccountId(accountId)
            .build();
        return accountServiceStub.getAccountInfo(request);
    }
}
```

## 🚀 빌드 및 실행

### 로컬 개발 환경

1. **사전 요구사항**
   ```bash
   - Java 17+
   - PostgreSQL 13+
   - Redis 6+
   - OpenBanking API 서버
   ```

2. **의존성 설치 및 빌드**
   ```bash
   ./gradlew clean build
   ```

3. **환경변수 설정**
   ```yaml
   # application.yml (Kubernetes ConfigMap에서 주입)
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/ssok_account
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
     data:
       redis:
         host: ${REDIS_HOST}
         port: ${REDIS_PORT}
   
   external:
     openbanking-service:
       base-url: ${OPENBANKING_BASE_URL}
       api-key: ${OPENBANKING_API_KEY}
     user-service:
       url: ${USER_SERVICE_URL}
   
   grpc:
     server:
       port: 9090
     client:
       user-service:
         address: ${USER_SERVICE_GRPC_ADDRESS}
   ```

4. **애플리케이션 실행**
   ```bash
   java -jar build/libs/ssok-account-service-1.0-SNAPSHOT.jar
   ```

### Docker 컨테이너 실행

1. **이미지 빌드**
   ```bash
   docker build -t ssok-account-service:latest .
   ```

2. **컨테이너 실행**
   ```bash
   docker run -p 8080:8080 -p 9090:9090 \
     -e DB_USERNAME=your_db_user \
     -e DB_PASSWORD=your_db_password \
     -e REDIS_HOST=redis-host \
     -e OPENBANKING_API_KEY=your_api_key \
     ssok-account-service:latest
   ```

## ⚡ 성능 최적화

### 비동기 처리
- **WebClient**: OpenBanking API 호출 시 Non-blocking I/O
- **CompletableFuture**: 다중 계좌 잔액 조회 시 병렬 처리
- **@Async**: 백그라운드 작업 비동기 실행

### 캐싱 전략
- **Redis**: 자주 조회되는 계좌 정보 캐싱
- **TTL 관리**: 잔액 정보는 짧은 TTL, 계좌 정보는 긴 TTL

### 데이터베이스 최적화
- **인덱스**: user_id, account_number, is_primary_account 인덱스
- **Connection Pool**: HikariCP를 통한 연결 풀 관리
- **Query 최적화**: JPA N+1 문제 방지

## 📊 모니터링 및 로깅

### 헬스체크
```
GET /actuator/health
```

### 메트릭 수집
```
GET /actuator/prometheus
```

### 로그 구조
```json
{
  "time": "2024-01-01T12:00:00.000+09:00",
  "level": "INFO",
  "logger": "kr.ssok.accountservice.service.AccountService",
  "thread": "http-nio-8080-exec-1",
  "message": "계좌 등록 완료. 사용자 ID: 12345, 계좌번호: ****-****-1234",
  "app": "ssok-account-service"
}
```

### 커스텀 메트릭
- `account.registration.total`: 계좌 등록 건수
- `account.balance.query.duration`: 잔액 조회 응답 시간
- `openbanking.api.call.total`: OpenBanking API 호출 건수
- `openbanking.api.error.total`: OpenBanking API 오류 건수

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### API 테스트 (Swagger UI)
```
http://localhost:8080/swagger-ui/index.html
```

### 통합 테스트
```bash
# TestContainers를 활용한 통합 테스트
./gradlew integrationTest
```

## 🚨 예외 처리

### 주요 예외 상황
- **계좌 미존재**: 요청한 계좌가 존재하지 않는 경우
- **중복 계좌**: 이미 등록된 계좌를 다시 등록하려는 경우
- **실명 불일치**: 계좌 소유자와 사용자 정보가 일치하지 않는 경우
- **주계좌 삭제 시도**: 주계좌를 삭제하려는 경우
- **OpenBanking API 오류**: 외부 API 호출 실패
- **네트워크 타임아웃**: 외부 서비스 응답 지연

### 응답 형식
```json
{
  "success": false,
  "code": 4200,
  "message": "요청하신 계좌가 존재하지 않습니다.",
  "data": null
}
```

### 오류 코드 체계
- **2200-2299**: 성공 응답
- **4200-4249**: 클라이언트 오류 (계좌 관련)
- **4250-4299**: 내부 서버 오류
- **5250-5299**: 외부 서비스 오류

## 🔒 보안 고려사항

### 데이터 보안
- **계좌번호 마스킹**: 로그 및 응답에서 계좌번호 일부 마스킹
- **API Key 관리**: OpenBanking API Key는 환경변수로 관리
- **실명 확인**: 본인 명의 계좌만 등록 가능

### 접근 제어
- **사용자 인증**: Gateway를 통한 JWT 토큰 검증
- **데이터 격리**: 사용자별 데이터 접근 제한
- **내부 API**: gRPC 통신은 내부 네트워크에서만 접근 가능

## 📋 TODO / 개선사항

- [ ] **계좌 연동 확대**: 더 많은 은행 지원
- [ ] **실시간 알림**: 계좌 잔액 변동 시 알림 기능
- [ ] **거래 내역 조회**: OpenBanking을 통한 거래 내역 제공
- [ ] **계좌 분석**: 사용자 계좌 사용 패턴 분석
- [ ] **배치 처리**: 대량 계좌 정보 동기화
- [ ] **계좌 그룹화**: 사용자 정의 계좌 그룹 관리
- [ ] **API Rate Limiting**: OpenBanking API 호출량 제한 관리
- [ ] **데이터 암호화**: 민감한 계좌 정보 암호화 저장

## 🤝 기여 가이드

1. Feature 브랜치 생성
2. 코드 작성 및 테스트
3. OpenBanking API 연동 테스트
4. Pull Request 생성
5. 코드 리뷰 및 머지

## 📞 문의

계좌 서비스 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

> **Note**: 이 서비스는 SSOK 마이크로서비스 아키텍처의 핵심 구성요소로, 실제 금융 거래를 위한 계좌 정보를 안전하게 관리합니다. 다른 서비스들과의 연동 정보는 [메인 README](../README.md)를 참조하세요.