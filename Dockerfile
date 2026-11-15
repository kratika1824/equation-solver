# build stage: compile and package the jar (tests are skipped here, run them with `mvn test`)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B package -DskipTests

# run stage: only the JRE and the jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/equation-solver-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
