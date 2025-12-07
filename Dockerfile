# ===================================
# Goukon Server - Multi-stage Build
# ===================================

# Stage 1: 의존성 캐시
FROM gradle:8.14-jdk21-jammy AS dependencies

WORKDIR /app

# 빌드 설정 파일 복사
COPY gradle.properties ./
COPY gradlew ./
COPY gradle gradle/
COPY build.gradle settings.gradle ./

# 의존성 다운로드 (캐시 레이어)
RUN ./gradlew dependencies --no-daemon --parallel \
    -Dorg.gradle.daemon.idletimeout=10000 \
    || true

# Stage 2: 빌드
FROM dependencies AS builder

COPY src src/

# JAR 빌드 (테스트 제외)
RUN ./gradlew bootJar \
    --no-daemon \
    --parallel \
    --build-cache \
    -x test

# Stage 3: 런타임
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# curl 설치 (헬스체크용)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 보안: non-root 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring && \
    chown -R spring:spring /app

# JAR 파일 복사
COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

USER spring:spring

EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 최적화 옵션
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:InitialRAMPercentage=50.0", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
