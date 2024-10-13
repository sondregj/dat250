FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
USER 1001

COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar", "--server.port=8080"]
