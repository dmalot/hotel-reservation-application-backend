#FROM maven:3.9.9-amazoncorretto-21-debian AS build
#
#WORKDIR /app
#
#COPY pom.xml .
#RUN mvn dependency:go-offline
#RUN mvn --version
#
#COPY src ./src
#RUN mvn -f /app/pom.xml clean package -DskipTests
#
#FROM openjdk:17-jdk-slim
#
#WORKDIR /app
#
#COPY --from=build /app/target/application-0.0.1-SNAPSHOT.jar .
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "/app/application-0.0.1-SNAPSHOT.jar"]

FROM maven:3.9.9-amazoncorretto-21-debian AS build

WORKDIR /app

# Copy only the pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the application source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# --- Second Stage: Runtime Image ---
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

# Copy only the JAR file from the build stage
COPY --from=build /app/target/application-0.0.1-SNAPSHOT.jar ./application.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]