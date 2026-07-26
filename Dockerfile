FROM maven:3.9.16-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S shopwise \
    && adduser -S shopwise -G shopwise

COPY --from=build --chown=shopwise:shopwise \
    /app/target/*.jar app.jar

EXPOSE 8080

USER shopwise

ENTRYPOINT ["java", "-jar", "app.jar"]