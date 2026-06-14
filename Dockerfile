FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/transac-aggr-api-app-*.jar app.jar

RUN addgroup --system appgroup && adduser --system --ingroup appgroup --uid 10000 appuser

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

