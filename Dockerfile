FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/transac-aggr-api-app-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

