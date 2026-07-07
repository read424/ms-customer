FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY target/app.jar app.jar

RUN addgroup -g 1000 -S app && \
    adduser -u 1000 -S app -G app

USER app

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]