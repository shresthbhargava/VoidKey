# ---- Build stage ----
# Using a separate build stage means the final image doesn't carry Maven,
# the full JDK, or your ~/.m2 dependency cache — only the compiled JAR and
# a slim JRE. This is the standard "multi-stage build" pattern; skipping it
# would produce an image several times larger for no benefit at runtime.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Downloading dependencies before copying source code means Docker can
# cache this layer — as long as pom.xml doesn't change, re-running the
# build won't re-download every dependency from Maven Central each time.
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kolkata", "-jar", "app.jar"]