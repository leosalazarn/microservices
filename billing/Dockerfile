FROM gradle:8.11-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :billing:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/billing/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
