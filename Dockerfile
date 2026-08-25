FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src
COPY --from=frontend-build /workspace/frontend/dist src/main/resources/static
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=backend-build /workspace/target/knowledge-network-1.0.0-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
