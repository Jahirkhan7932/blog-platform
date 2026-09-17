# ========== Stage 1: Build ==========
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ========== Stage 2: Runtime ==========
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]