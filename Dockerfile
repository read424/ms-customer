# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace
COPY . .
RUN ./mvnw clean package -DskipTests=true

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -g 1000 -S app && adduser -u 1000 -S app -G app
COPY --from=builder /workspace/target/*.jar app.jar
RUN chown -R app:app /app
USER app
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
