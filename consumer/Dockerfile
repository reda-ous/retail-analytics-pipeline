# Build stage: needs the whole Maven reactor (parent pom + sibling modules),
# since `consumer` depends on `common` and Maven must resolve the full module graph.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY common common
COPY consumer consumer
COPY producer/pom.xml producer/pom.xml

RUN mvn -q -pl consumer -am package -DskipTests

# Runtime stage: just the JRE and the built jar, nothing else from the build.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/consumer/target/consumer-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
