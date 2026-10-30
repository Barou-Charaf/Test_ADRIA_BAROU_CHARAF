FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8081
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=10 \
  CMD wget -q -O - http://localhost:8081/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
