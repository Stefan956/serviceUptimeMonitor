# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy all POM files first so Maven can resolve dependencies as a cached layer.
# Re-downloading only happens when a pom.xml changes.
COPY pom.xml                        ./pom.xml
COPY monitoring-service/pom.xml     ./monitoring-service/pom.xml
COPY alert-service/pom.xml          ./alert-service/pom.xml
COPY dashboard-service/pom.xml      ./dashboard-service/pom.xml

RUN mvn dependency:go-offline -pl monitoring-service -am -q

# Copy source and build the fat JAR (skip tests — they run in CI)
COPY monitoring-service/src ./monitoring-service/src
RUN mvn package -pl monitoring-service -am -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/monitoring-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
