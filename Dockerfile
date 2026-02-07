# ---------- 1. 자바 빌드 (로컬) ----------
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Gradle 캐시 최적화
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사 & 빌드
COPY src src
RUN ./gradlew clean build -x test --no-daemon


# ---------- 2. 빌드 파일 동작 (로컬, AWS 공용) ----------
FROM amazoncorretto:17-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
