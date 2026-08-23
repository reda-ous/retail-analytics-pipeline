# Build stage: needs the whole Maven reactor (parent pom + sibling modules),
# since `producer` depends on `common` and Maven must resolve the full module graph.
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY common common
COPY producer producer
COPY consumer/pom.xml consumer/pom.xml

RUN mvn -q -pl producer -am package -DskipTests

# Runtime stage: just the JRE and the built jar, nothing else from the build.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/producer/target/producer-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
