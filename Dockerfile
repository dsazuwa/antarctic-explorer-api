# TODO: jdk for dev, jre for prod
FROM eclipse-temurin:17-jdk-alpine

COPY wait-for-it.sh ./
COPY docker-entrypoint.sh ./
RUN chmod +x wait-for-it.sh docker-entrypoint.sh

COPY target/api-0.0.1-SNAPSHOT.jar api.jar

ENTRYPOINT ["./docker-entrypoint.sh"]
CMD ["java", "-jar", "/api.jar"]