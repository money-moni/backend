pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS = credentials('DOCKER_USER')
        DOCKER_USER = "${DOCKER_CREDENTIALS_USR}"
        DOCKER_PASS = "${DOCKER_CREDENTIALS_PSW}"
        GIT_CREDENTIALS = credentials('GITHUB_USER')
        GIT_USER = "${GIT_CREDENTIALS_USR}"
        GIT_PASS = "${GIT_CREDENTIALS_PSW}"
    }

    stages {
        stage('Prepare Environment') {
            steps {
                sh 'chmod +x setup.sh'
                sh './setup.sh'
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    // 변경된 파일 감지 (이전 커밋과 현재 커밋 비교)
                    def changedFiles = sh(script: "git diff --name-only HEAD~1 HEAD || git diff --name-only", returnStdout: true).trim()

                    echo "Changed files: ${changedFiles}"

                    // common 모듈 변경 여부 확인
                    env.COMMON_CHANGED = changedFiles.contains('ssok-common/') || changedFiles.contains('Jenkinsfile') || changedFiles.contains('build.gradle') || changedFiles.contains('settings.gradle') ? 'true' : 'false'

                    // 각 서비스별 변경 여부 확인
                    env.CHANGED_ACCOUNT_SERVICE = changedFiles.contains('ssok-account-service/') ? 'true' : 'false'
                    env.CHANGED_USER_SERVICE = changedFiles.contains('ssok-user-service/') ? 'true' : 'false'
                    env.CHANGED_TRANSFER_SERVICE = changedFiles.contains('ssok-transfer-service/') ? 'true' : 'false'
                    env.CHANGED_NOTIFICATION_SERVICE = changedFiles.contains('ssok-notification-service/') ? 'true' : 'false'
                    env.CHANGED_GATEWAY = changedFiles.contains('ssok-gateway-service/') ? 'true' : 'false'
                    env.CHANGED_BLUETOOTH_SERVICE = changedFiles.contains('ssok-bluetooth-service/') ? 'true' : 'false'

                    // common 모듈이 변경되면 모든 서비스 재빌드
                    if (changedFiles.contains('ssok-common/') || changedFiles.contains('Jenkinsfile') || changedFiles.contains('build.gradle') || changedFiles.contains('settings.gradle')) {
                        echo "Common module or build configuration changed. Rebuilding all services."
                        env.CHANGED_ACCOUNT_SERVICE = 'true'
                        env.CHANGED_USER_SERVICE = 'true'
                        env.CHANGED_TRANSFER_SERVICE = 'true'
                        env.CHANGED_NOTIFICATION_SERVICE = 'true'
                        env.CHANGED_GATEWAY = 'true'
                        env.CHANGED_BLUETOOTH_SERVICE = 'true'
                    }

                    echo "Account Service changed: ${env.CHANGED_ACCOUNT_SERVICE}"
                    echo "User Service changed: ${env.CHANGED_USER_SERVICE}"
                    echo "Transfer Service changed: ${env.CHANGED_TRANSFER_SERVICE}"
                    echo "Notification Service changed: ${env.CHANGED_NOTIFICATION_SERVICE}"
                    echo "Gateway changed: ${env.CHANGED_GATEWAY}"
                    echo "Bluetooth Service changed: ${env.CHANGED_BLUETOOTH_SERVICE}"
                }
            }
        }

        // Notification Service를 위한 Firebase SDK 파일 준비
        stage('Prepare Firebase SDK') {
            when { expression { return env.CHANGED_NOTIFICATION_SERVICE == 'true' } }
            steps {
                script {
                    // Firebase 디렉토리 생성
                    sh 'mkdir -p ssok-notification-service/src/main/resources/firebase'
                    
                    // Jenkins에 저장된 Firebase SDK 파일 복사
                    sh 'cp /var/jenkins_home/env/firebase-adminsdk.json ssok-notification-service/src/main/resources/firebase/'
                    
                    // 파일이 제대로 복사되었는지 확인
                    sh 'ls -la ssok-notification-service/src/main/resources/firebase/'
                }
            }
        }

        // 모든 서비스 빌드를 한 번에 수행
        stage('Build All Services') {
            when { expression { return env.COMMON_CHANGED == 'true' || env.CHANGED_ACCOUNT_SERVICE == 'true' || env.CHANGED_USER_SERVICE == 'true' || env.CHANGED_TRANSFER_SERVICE == 'true' || env.CHANGED_NOTIFICATION_SERVICE == 'true' || env.CHANGED_GATEWAY == 'true' || env.CHANGED_BLUETOOTH_SERVICE == 'true' } }
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew clean build --refresh-dependencies -x test'
                sh 'ls -la target/' // 빌드된 JAR 파일 확인
            }
        }

        stage('Docker Login') {
            steps {
                sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
            }
        }

        // 계정 서비스 배포
        stage('Deploy Account Service') {
            when { expression { return env.CHANGED_ACCOUNT_SERVICE == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-account-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-account-service/pipeline.sh'
                }
                echo "Account Service deploy completed"
            }
        }

        // 사용자 서비스 배포
        stage('Deploy User Service') {
            when { expression { return env.CHANGED_USER_SERVICE == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-user-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-user-service/pipeline.sh'
                }
                echo "User Service deploy completed"
            }
        }

        // 송금 서비스 배포
        stage('Deploy Transfer Service') {
            when { expression { return env.CHANGED_TRANSFER_SERVICE == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-transfer-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-transfer-service/pipeline.sh'
                }
                echo "Transfer Service deploy completed"
            }
        }

        // 알림 서비스 배포
        stage('Deploy Notification Service') {
            when { expression { return env.CHANGED_NOTIFICATION_SERVICE == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-notification-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-notification-service/pipeline.sh'
                }
                echo "Notification Service deploy completed"
            }
        }

        // 게이트웨이 배포
        stage('Deploy Gateway-service') {
            when { expression { return env.CHANGED_GATEWAY == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-gateway-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-gateway-service/pipeline.sh'
                }
                echo "Gateway deploy completed"
            }
        }

        // 블루투스 서비스 배포
        stage('Deploy Bluetooth Service') {
            when { expression { return env.CHANGED_BLUETOOTH_SERVICE == 'true' } }
            steps {
                withEnv(["DOCKER_USER=${DOCKER_USER}", "GIT_PASS=${GIT_PASS}", "WORKSPACE=${WORKSPACE}"]) {
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-bluetooth-service/deploy.sh'
                    sh 'cd ssok-deploy && ./jenkins/ssok-app/ssok-bluetooth-service/pipeline.sh'
                }
                echo "Bluetooth Service deploy completed"
            }
        }
    }

    post {
        always {
            echo 'Cleaning up workspace...'
            // cleanWs() 주석 처리 - 다른 스테이지에서 빌드 결과물을 사용할 수 있도록
            sh 'docker logout'
        }
        success {
            echo 'CI/CD Pipeline completed successfully!'
            cleanWs() // 성공 시에만 워크스페이스 정리
        }
        failure {
            echo 'CI/CD Pipeline failed!'
        }
    }
}