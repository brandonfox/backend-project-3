#Build stage
FROM maven:alpine as build-stage
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /app/src/
RUN mvn package -DskipTests

#Production stage
FROM openjdk:8-jre-alpine as production-stage
COPY --from=build-stage /app/target /app
WORKDIR /app
ENTRYPOINT ["java","-jar","./sos-0.0.1-SNAPSHOT.jar"]