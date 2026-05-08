# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml                        ./pom.xml
COPY monitoring-service/pom.xml     ./monitoring-service/pom.xml
COPY alert-service/pom.xml          ./alert-service/pom.xml
COPY dashboard-service/pom.xml      ./dashboard-service/pom.xml

RUN mvn dependency:go-offline -pl dashboard-service -am -q

COPY dashboard-service/src ./dashboard-service/src
RUN mvn package -pl dashboard-service -am -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/dashboard-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
