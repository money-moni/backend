FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew :ssok-common:build :ssok-bluetooth-service:build --refresh-dependencies -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/ssok-bluetooth-service/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dspring.config.location=file:/config/application.yml", "-jar", "/app/app.jar"]
