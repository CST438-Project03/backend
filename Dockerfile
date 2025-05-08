# --- Build Stage ---
    FROM gradle:8.5-jdk17 AS builder

    WORKDIR /app
    
    # Copy everything into the container (you can optimize this later)
    COPY . .
    
    # Build the Spring Boot JAR using the Gradle wrapper
    RUN ./gradlew bootJar --no-daemon
    
    
    # --- Run Stage ---
    FROM eclipse-temurin:17-jre
    
    WORKDIR /app
    
    # Copy the built JAR from the previous stage
    COPY --from=builder /app/build/libs/proj3-0.0.1-SNAPSHOT.jar app.jar

    
    # Expose the port your Spring Boot app runs on
    EXPOSE 8080
    
    # Run the application
    ENTRYPOINT ["java", "-jar", "app.jar"]
    