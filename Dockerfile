FROM maven:3.9.9-amazoncorretto-21-alpine AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline
RUN mvn --version

COPY src ./src
RUN mvn -f /app/pom.xml clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=build /app/target/application-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application-0.0.1-SNAPSHOT.jar"]