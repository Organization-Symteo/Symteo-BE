FROM amazoncorretto:17-alpine

# 컨테이너 내 작업 디렉토리 설정
WORKDIR /app

# GitHub Actions 빌드 단계에서 생성된 jar 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 실행 권한 부여 및 포트 설정 (필요시)
EXPOSE 8080

# 어플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
