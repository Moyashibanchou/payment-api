# Build stage
FROM eclipse-temurin:17-jdk-focal AS build
WORKDIR /app
COPY . .
RUN chmod +x ./mvnw
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app
COPY --from=build /app/target/payment-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
