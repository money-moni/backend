# SSOK User Service

> 사용자 관리, 인증/인가, 프로필 관리를 담당하는 마이크로서비스

## 📋 개요

SSOK User Service는 SSOK 플랫폼의 **사용자 관리 및 인증 시스템**을 담당하는 핵심 서비스입니다. 회원가입부터 로그인, 프로필 관리, PIN 코드 관리까지 사용자와 관련된 모든 기능을 제공합니다.

### 주요 기능

- **회원가입 & 인증**: 휴대폰 SMS 인증을 통한 안전한 회원가입
- **로그인 & 보안**: PIN 코드 기반 로그인 및 JWT 토큰 관리
- **프로필 관리**: AWS S3 연동 프로필 이미지 업로드/관리
- **약관 관리**: 서비스 이용약관 조회 및 동의 관리
- **gRPC 통신**: 다른 서비스와의 고성능 내부 통신

## 🏗️ 아키텍처

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Gateway       │    │   User Service   │    │  External APIs  │
│                 │    │                  │    │                 │
│ • JWT 검증       │◄──►│ • 사용자 관리     │◄──►│ • Aligo SMS     │
│ • 요청 라우팅     │    │ • 인증/인가       │    │ • AWS S3        │
│                 │    │ • 프로필 관리     │    │                 │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │
                                │ gRPC
                                ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   PostgreSQL    │    │      Redis       │    │ Other Services  │
│                 │    │                  │    │                 │
│ • 사용자 정보     │    │ • JWT 토큰        │    │ • Account       │
│ • 프로필 이미지    │    │ • 인증 코드       │    │ • Transfer      │
│ • 약관 정보       │    │ • 세션 관리       │    │ • Bluetooth     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

## 🔧 기술 스택

| 분류 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.4.4, Spring Security |
| **Database** | PostgreSQL (주 DB), Redis (캐시/세션) |
| **Authentication** | JWT Token, PIN Code |
| **Communication** | REST API, gRPC, OpenFeign |
| **Cloud Storage** | AWS S3 |
| **External APIs** | Aligo SMS Service |
| **Documentation** | OpenAPI 3.0 (Swagger) |
| **Monitoring** | Micrometer, Actuator |
| **Build** | Gradle |

## 📁 프로젝트 구조

```
ssok-user-service/
├── src/main/java/kr/ssok/userservice/
│   ├── client/                 # 외부 서비스 클라이언트
│   │   ├── AligoClient.java    # SMS 발송 클라이언트
│   │   └── BankClient.java     # 은행 서비스 클라이언트
│   ├── config/                 # 설정 클래스
│   │   ├── SecurityConfig.java # Spring Security 설정
│   │   ├── S3Config.java      # AWS S3 설정
│   │   ├── RedisConfig.java   # Redis 설정
│   │   └── SwaggerConfig.java # API 문서 설정
│   ├── controller/            # REST API 컨트롤러
│   │   ├── AuthController.java        # 인증 관련 API
│   │   ├── UserController.java        # 사용자 관련 API
│   │   ├── ProfileController.java     # 프로필 관리 API
│   │   └── TermsController.java       # 약관 관리 API
│   ├── dto/                   # 데이터 전송 객체
│   │   ├── request/           # 요청 DTO
│   │   └── response/          # 응답 DTO
│   ├── entity/                # JPA 엔티티
│   │   ├── User.java         # 사용자 엔티티
│   │   ├── ProfileImage.java # 프로필 이미지 엔티티
│   │   └── Terms.java        # 약관 엔티티
│   ├── exception/             # 예외 처리
│   │   ├── UserException.java
│   │   ├── UserExceptionHandler.java
│   │   └── UserResponseStatus.java
│   ├── grpc/                  # gRPC 서버 구현
│   │   └── server/
│   ├── security/              # 보안 관련
│   │   └── jwt/
│   │       └── JwtTokenProvider.java
│   ├── service/               # 비즈니스 로직
│   │   ├── AuthService.java          # 인증 서비스
│   │   ├── UserService.java          # 사용자 서비스
│   │   ├── ProfileService.java       # 프로필 서비스
│   │   ├── TermsService.java         # 약관 서비스
│   │   └── S3FileService.java        # S3 파일 서비스
│   └── repository/            # 데이터 접근 계층
├── src/main/resources/
│   └── logback-spring.xml     # 로깅 설정
├── build.gradle              # 빌드 설정
└── Dockerfile                # 컨테이너 이미지 빌드
```

## 🗄️ 데이터베이스 스키마

### Users 테이블
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    birth_date VARCHAR(10) NOT NULL,
    pin_code VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Profile Images 테이블
```sql
CREATE TABLE profile_image (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    stored_filename VARCHAR(255),
    url TEXT,
    content_type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Terms 테이블
```sql
CREATE TABLE terms (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🔌 API 엔드포인트

### 인증 관련 (`/api/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/login` | 로그인 (PIN 코드 인증) | ❌ |
| POST | `/refresh` | JWT 토큰 갱신 | ❌ |
| POST | `/logout` | 로그아웃 | ✅ |
| POST | `/background` | 앱 백그라운드 전환 | ✅ |
| POST | `/foreground` | 앱 포그라운드 복귀 | ❌ |
| GET | `/internal/{userId}` | 사용자 인증 정보 조회 (내부용) | ❌ |

### 사용자 관리 (`/api/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/signup` | 회원가입 | ❌ |
| POST | `/phone` | 휴대폰 인증 코드 발송 | ❌ |
| POST | `/phone/verify` | 인증 코드 확인 | ❌ |
| POST | `/phone/verify-with-user-check` | 인증 코드 확인 + 기존 사용자 체크 | ❌ |
| POST | `/pin/{userId}` | PIN 변경용 휴대폰 인증 | ❌ |
| POST | `/pin/verify` | PIN 변경용 인증 코드 확인 | ✅ |
| PATCH | `/pin` | PIN 코드 변경 | ❌ |
| PATCH | `/pin/existing-user` | 기존 사용자 PIN 재등록 | ❌ |
| GET | `/info` | 사용자 정보 조회 | ✅ |

### 프로필 관리 (`/api/profiles`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | 프로필 이미지 업로드 | ✅ |
| GET | `/` | 프로필 이미지 조회 | ✅ |
| PUT | `/` | 프로필 이미지 수정 | ✅ |
| DELETE | `/` | 프로필 이미지 삭제 | ✅ |

### 약관 관리 (`/api/terms`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | 약관 목록 조회 | ❌ |
| GET | `/{termsId}` | 약관 상세 조회 | ❌ |

## 🔒 보안 및 인증

### JWT 토큰 관리
- **Access Token**: 30분 유효 (API 접근용)
- **Refresh Token**: 7일 유효 (토큰 갱신용)
- **토큰 블랙리스트**: Redis를 통한 로그아웃 토큰 관리
- **PIN 코드**: BCrypt 해시화 저장

### 인증 플로우
1. **회원가입**: 휴대폰 SMS 인증 → 사용자 정보 입력 → PIN 설정
2. **로그인**: 사용자 ID + PIN 코드 → JWT 토큰 발급
3. **토큰 갱신**: Refresh Token → 새로운 Access Token 발급
4. **로그아웃**: Access Token 블랙리스트 추가 + Refresh Token 삭제

### 보안 기능
- **로그인 시도 제한**: Redis를 통한 브루트포스 공격 방지
- **토큰 검증**: 만료, 무효성, 블랙리스트 체크
- **입력값 검증**: Jakarta Validation을 통한 데이터 유효성 검사

## 📱 외부 서비스 연동

### Aligo SMS 서비스
```java
@FeignClient(name = "notification-service")
public interface AligoClient {
    @PostMapping("/api/notification/verify")
    BaseResponse<Void> sendVerificationCode(@RequestBody AligoVerificationRequestDto requestDto);
}
```

### AWS S3 연동
- **프로필 이미지 업로드**: 멀티파트 파일 → S3 저장 → URL 반환
- **이미지 관리**: 업로드, 수정, 삭제 기능
- **파일 검증**: 파일 크기(5MB 제한), 이미지 형식 검증

## 🚀 빌드 및 실행

### 로컬 개발 환경

1. **사전 요구사항**
   ```bash
   - Java 17+
   - PostgreSQL 13+
   - Redis 6+
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
       url: jdbc:postgresql://localhost:5432/ssok_user
       username: ${DB_USERNAME}
       password: ${DB_PASSWORD}
     data:
       redis:
         host: ${REDIS_HOST}
         port: ${REDIS_PORT}
   
   jwt:
     secret: ${JWT_SECRET}
     access-token-validity-in-seconds: 1800
     refresh-token-validity-in-seconds: 604800
   
   aws:
     s3:
       bucket: ${S3_BUCKET_NAME}
       region: ${AWS_REGION}
   ```

4. **애플리케이션 실행**
   ```bash
   java -jar build/libs/ssok-user-service-1.0-SNAPSHOT.jar
   ```

### Docker 컨테이너 실행

1. **이미지 빌드**
   ```bash
   docker build -t ssok-user-service:latest .
   ```

2. **컨테이너 실행**
   ```bash
   docker run -p 8080:8080 \
     -e DB_USERNAME=your_db_user \
     -e DB_PASSWORD=your_db_password \
     -e REDIS_HOST=redis-host \
     -e JWT_SECRET=your_jwt_secret \
     ssok-user-service:latest
   ```

## 🔍 gRPC 내부 통신

다른 마이크로서비스에서 사용자 정보가 필요할 때 gRPC를 통해 효율적으로 통신합니다.

### gRPC 서비스 정의
```protobuf
service UserService {
    rpc GetUserInfo(UserIdRequest) returns (UserInfoResponse);
}

message UserIdRequest {
    string userId = 1;
}

message UserInfoResponse {
    string username = 1;
    string phoneNumber = 2;
    string profileImage = 3;
}
```

### 사용 예시 (다른 서비스에서)
```java
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
  "logger": "kr.ssok.userservice.controller.AuthController",
  "thread": "http-nio-8080-exec-1",
  "message": "로그인 요청. 사용자 ID: 12345",
  "app": "ssok-user-service"
}
```

## 🧪 테스트

### 단위 테스트 실행
```bash
./gradlew test
```

### API 테스트 (Swagger UI)
```
http://localhost:8080/swagger-ui/index.html
```

## 🚨 예외 처리

### 주요 예외 상황
- **인증 실패**: PIN 코드 불일치, 토큰 만료/무효
- **회원가입 실패**: 중복 사용자, 유효성 검증 실패
- **SMS 발송 실패**: 외부 API 오류, 네트워크 문제
- **파일 업로드 실패**: 파일 크기 초과, 잘못된 형식
- **데이터베이스 오류**: 연결 실패, 제약조건 위반

### 응답 형식
```json
{
  "success": false,
  "code": 4000,
  "message": "유효하지 않은 PIN 번호입니다.",
  "data": null
}
```

## 📋 TODO / 개선사항

- [ ] **소셜 로그인 연동**: Google, Apple, Kakao 로그인 지원
- [ ] **2FA 인증**: TOTP 기반 2단계 인증 추가
- [ ] **비밀번호 정책**: PIN 복잡도 규칙 강화
- [ ] **사용자 활동 로그**: 로그인 이력, 디바이스 관리
- [ ] **계정 복구**: 휴대폰 번호 변경 시 계정 복구 플로우
- [ ] **프로필 확장**: 추가 사용자 정보 필드 지원

## 🤝 기여 가이드

1. Feature 브랜치 생성
2. 코드 작성 및 테스트
3. Pull Request 생성
4. 코드 리뷰 및 머지

## 📞 문의

개발팀 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

> **Note**: 이 서비스는 SSOK 마이크로서비스 아키텍처의 핵심 구성요소입니다. 다른 서비스들과의 연동 정보는 [메인 README](../README.md)를 참조하세요.