FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 -S app && \
    adduser -u 1000 -S app -G app

COPY target/app.jar app.jar

RUN chown -R app:app /app

USER app

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]