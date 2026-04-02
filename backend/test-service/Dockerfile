# Сборка
FROM gradle:8.14-jdk17 AS build
WORKDIR /app

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src

# собираем JAR
RUN gradle bootJar --no-daemon

# Запуск
FROM eclipse-temurin:17
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar
CMD ["java", "-jar", "app.jar"]