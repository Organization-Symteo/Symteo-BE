########## build stage ##########
FROM gradle:8.7-jdk17-alpine AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies || true

COPY src ./src
RUN gradle --no-daemon clean bootJar

########## runtime stage ##########
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
