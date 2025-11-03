# Multi-stage Dockerfile for building and running the Spring Boot application
# Builds with the Maven wrapper and runs the resulting fat jar.

FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app

# Copy wrapper and pom first to cache dependencies
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Ensure the mvnw wrapper is executable (Windows git clients may strip exec bit)
RUN chmod +x ./mvnw || true

# Pre-download dependencies (uses mvnw inside the container)
RUN ./mvnw -B -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy jar produced by the build stage
ARG JAR_FILE=target/canteen-management-system-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/${JAR_FILE} /app/app.jar

# Allow Render (or other PaaS) to bind the port via PORT env var
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar /app/app.jar"]
