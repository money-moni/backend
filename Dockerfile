FROM openjdk:17-jdk-slim as builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
