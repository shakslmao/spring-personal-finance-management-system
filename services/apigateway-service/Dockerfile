FROM openjdk:17-jdk-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY target/apigateway-service-0.0.1-SNAPSHOT.jar /app/apigateway.jar

EXPOSE 8222

ENTRYPOINT [ "java", "-jar", "/app/apigateway.jar" ]

