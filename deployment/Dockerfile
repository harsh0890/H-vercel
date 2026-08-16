FROM openjdk:21-jre-slim

RUN apt-get update && apt-get install -y \
    docker.io \
    maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY target/deploy-vercel-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

CMD ["java", "-jar", "app.jar"] 