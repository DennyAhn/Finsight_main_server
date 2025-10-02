# ================= STAGE 1: Build the application =================
# Java 17 버전을 빌드 환경으로 사용합니다.
FROM openjdk:17-jdk-slim AS builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# Gradle 관련 파일들을 먼저 복사하여 의존성 캐싱을 활용합니다.
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 의존성을 다운로드합니다.
RUN ./gradlew dependencies

# 소스 코드를 복사합니다.
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드합니다. (-x test로 테스트는 제외)
RUN ./gradlew build -x test

# ================= STAGE 2: Create the final image =================
# 더 가벼운 JRE 버전을 실행 환경으로 사용합니다.
FROM openjdk:17-jre-slim

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 빌드 스테이지에서 생성된 .jar 파일을 복사합니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행 포트를 노출합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어입니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
