
FROM maven:3.9.9-eclipse-temurin-17


WORKDIR /app

COPY target/flight-service-0.0.1-SNAPSHOT.jar app.jar

CMD ["java","-Dspring.profiles.active=docker","-jar","app.jar"]