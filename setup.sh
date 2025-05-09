#!/bin/bash
# Clone ssok-deploy repository to get build scripts
git clone https://${GIT_PASS}@github.com/Team-SSOK/ssok-deploy.git

# Make all scripts executable
chmod +x ssok-deploy/jenkins/utils.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-account-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-account-service/pipeline.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-user-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-user-service/pipeline.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-transfer-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-transfer-service/pipeline.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-notification-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-notification-service/pipeline.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-gateway-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-gateway-service/pipeline.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-bluetooth-service/deploy.sh
chmod +x ssok-deploy/jenkins/ssok-app/ssok-bluetooth-service/pipeline.sh

echo "All scripts are now executable!"
