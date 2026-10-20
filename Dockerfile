# Multi-stage Docker build for SkillMatch
# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build production executable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal, secure runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ensure resume uploads directory exists
RUN mkdir -p /app/uploads/resumes

# Copy compiled jar from build stage
COPY --from=build /app/target/skillmatch-1.0.0.jar app.jar

# Cloud platforms dynamically assign PORT env variable
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
