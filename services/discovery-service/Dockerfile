FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY target/discovery-service-0.0.1-SNAPSHOT.jar /app/discovery-service.jar

EXPOSE 8761

ENTRYPOINT [ "java", "-jar", "/app/discovery-service.jar" ]