FROM gradle:8.11-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :eureka-server:bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/eureka-server/build/libs/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
