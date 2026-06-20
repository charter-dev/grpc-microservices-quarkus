############################
# Stage 1: Build
############################
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first (layer cache for dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

############################
# Stage 2: Runtime
############################
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the Quarkus fast-jar
COPY --from=build /app/target/quarkus-app/lib/           ./lib/
COPY --from=build /app/target/quarkus-app/*.jar          ./
COPY --from=build /app/target/quarkus-app/app/           ./app/
COPY --from=build /app/target/quarkus-app/quarkus/       ./quarkus/

EXPOSE 8081 9000

ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENV QUARKUS_PROFILE=docker

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
