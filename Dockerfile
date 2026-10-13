# Multi-stage Docker build for Factory Management Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build jar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled jar
COPY --from=build /app/target/*.jar app.jar

# Expose port
ENV PORT=8080
EXPOSE 8080

# Run Spring Boot with dynamic PORT binding
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-8080} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
