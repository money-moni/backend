# SSOK 백엔드

## 개요
금융 송금 서비스의 백 엔드 코드베이스

## 프로젝트 구조
- `ssok-common`: 공통 라이브러리
- `ssok-account-service`: 계정 서비스
- `ssok-user-service`: 사용자 서비스
- `ssok-transfer-service`: 송금 서비스
- `ssok-notification-service`: 알림 서비스
- `ssok-gateway`: API 게이트웨이
- `ssok-bluetooth-service`: 블루투스 서비스

## Docker 빌드 및 실행

각 서비스는 개별 Dockerfile을 포함하고 있습니다. 서비스별 Docker 이미지를 빌드하려면:

```bash
# 루트 디렉토리에서 전체 서비스 빌드
docker build -t ssok-backend:latest .

# 특정 서비스만 빌드 (예: 계정 서비스)
docker build -f ssok-account-service/Dockerfile -t ssok-account-service:latest .

# 이미지 실행
docker run -p 8080:8080 ssok-account-service:latest
```

## CI/CD

develop 브랜치에 push 또는 merge가 발생하면 Jenkins CI/CD 파이프라인이 자동으로 실행됩니다.
변경된 서비스만 빌드 및 배포되도록 설정되어 있습니다.

## 참고 자료 
배포 관련 설정은 [ssok-deploy](https://github.com/Team-SSOK/ssok-deploy) 저장소를 참고하세요.
