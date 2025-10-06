# ================= STAGE 1: Build the application =================
# Java 17 버전을 빌드 환경으로 사용합니다.
FROM eclipse-temurin:17-jdk-jammy AS builder

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# Gradle 설치
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    mv gradle-8.5 /opt/gradle && \
    rm gradle-8.5-bin.zip && \
    apt-get clean

ENV GRADLE_HOME=/opt/gradle
ENV PATH=$PATH:$GRADLE_HOME/bin

# 빌드 파일 복사
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# 애플리케이션 빌드 (-x test로 테스트 제외)
RUN gradle build -x test --no-daemon

# ================= STAGE 2: Create the final image =================
# 더 가벼운 JRE 버전을 실행 환경으로 사용합니다.
FROM eclipse-temurin:17-jre-jammy

# 작업 디렉토리를 /app으로 설정합니다.
WORKDIR /app

# 빌드 스테이지에서 생성된 .jar 파일을 복사합니다.
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행 포트를 노출합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어입니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
