<div align="center">
    <img width="850px" alt="SSOK" src="https://github.com/user-attachments/assets/e7bf97d5-f088-4a6e-acc0-e90235779d9d">
    </a>
    <h3>"블루투스 기반 간편 송금 서비스 SSOK"</h3> 
</div>

</br>

---

</br>

## 📋 개요
> 블루투스 기반 송금 기능을 제공하는 금융 서비스 **SSOK**의 백엔드 저장소입니다.

**SSOK**은 금융 서비스 생태계의 **채널계**를 담당하고 있으며,  
사용자 요청을 처리하고 계정계 및 대외계와의 연동을 통해 실질적인 송금 기능을 제공합니다.

- 핀번호 기반 사용자 인증/인가
- 송금 요청 생성 및 처리
- 블루투스 송금 대상 매칭
- 송금 결과에 따른 알림 전송
- 직접 구현한 외부 금융기관([OpenBanking](https://github.com/Team-SSOK/ssok-openbanking))과의 API 연동

이를 통해 사용자에게 **간편하고 안전한 송금 경험**을 제공합니다.

</br>

작성중..

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
