
# Stage 1: Build Heartbeat
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

# Stage 2: Run Heartbeat
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/heartbeat-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
