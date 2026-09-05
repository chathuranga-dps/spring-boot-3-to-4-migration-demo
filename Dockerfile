# MIGRATION-DEMO: Runtime upgraded from Java 17 to Java 21 for Spring Boot 4.
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR file
COPY target/spring-boot-migration-demo-1.0.0.jar app.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
