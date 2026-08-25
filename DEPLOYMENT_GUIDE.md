# Deployment Guide

## Prerequisites
- Docker 24+
- Docker Compose v2+
- A Linux host or Docker-compatible environment

## 1. Prepare environment
Copy the example environment file and update secrets:

```bash
cp .env.example .env
```

Set secure values for:
- `APP_JWT_SECRET`
- `POSTGRES_PASSWORD`
- `GRAFANA_ADMIN_PASSWORD`

The production profile requires database credentials and the JWT secret. The app fails fast when these are missing.

## 2. Build and start services
```bash
docker compose up --build -d
```

## 3. Verify deployment
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/
curl http://localhost:9090/targets
```

## 4. Useful commands
```bash
docker compose ps
docker compose logs -f app
docker compose down
```

## 5. Production notes
- Use a managed PostgreSQL service instead of the local container in production.
- Put the application behind a reverse proxy with TLS termination.
- Rotate secrets regularly and avoid committing `.env` files.
- Monitor application health via `/actuator/health` and Prometheus/Grafana.
- The React frontend and Spring Boot API are served by the same application container on port 8080.
