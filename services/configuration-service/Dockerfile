FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY target/configuration-service-0.0.1-SNAPSHOT.jar /app/configuration-service.jar

EXPOSE 8888

ENTRYPOINT [ "java", "-jar", "/app/configuration-service.jar" ]