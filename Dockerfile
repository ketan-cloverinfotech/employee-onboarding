# Build the Spring Boot application.
FROM maven:3.9.11-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean package -DskipTests

# Run only the compiled application in a smaller image.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S onboarding && adduser -S onboarding -G onboarding
COPY --from=build /workspace/target/employee-onboarding-1.0.0.jar app.jar
USER onboarding
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
