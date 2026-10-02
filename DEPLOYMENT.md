# KnowledgeNetwork Production Deployment Guide

This guide describes how to configure, package, deploy, and operate **KnowledgeNetwork** in a production environment.

---

## 1. System Architecture

```text
       ┌──────────────────────────────────────────────┐
       │             Internet / Clients               │
       └──────────────────────┬───────────────────────┘
                              │ HTTPS (Port 443)
                              ▼
       ┌──────────────────────────────────────────────┐
       │        Reverse Proxy / Ingress Gateway       │
       │        (NGINX / Caddy / AWS ALB / Traefik)   │
       └──────────────────────┬───────────────────────┘
                              │ HTTP (Port 8080)
                              ▼
       ┌──────────────────────────────────────────────┐
       │         KnowledgeNetwork Container           │
       │  ┌────────────────────────────────────────┐  │
       │  │  Vite React SPA Static Files (Dist)    │  │
       │  ├────────────────────────────────────────┤  │
       │  │  Spring Boot REST API (Port 8080)      │  │
       │  └────────────────────────────────────────┘  │
       └──────────────────────┬───────────────────────┘
                              │ Internal Network (Port 5432)
                              ▼
       ┌──────────────────────────────────────────────┐
       │       PostgreSQL 16 Database Instance        │
       └──────────────────────────────────────────────┘
```

---

## 2. Production Prerequisites

* **Runtime Host**: Linux environment (Ubuntu 22.04 LTS recommended) or Managed Container Service (AWS ECS, GCP Cloud Run, Azure Container Apps).
* **Database**: PostgreSQL 16 instance with persistent SSD storage and automated backup snapshotting.
* **Domain & TLS**: Valid SSL/TLS domain certificate (Let's Encrypt / AWS ACM) terminating at the reverse proxy.
* **Secrets Engine**: Production environment secret store (AWS Secrets Manager, GCP Secret Manager, or system environment variables).

---

## 3. Production Environment Configuration

All production configuration must be supplied via environment variables (`SPRING_PROFILES_ACTIVE=prod`). **Never commit real passwords, JWT secrets, or SMTP credentials to source code.**

### Required Environment Variables

| Variable | Description | Example / Default | Secret? |
| :--- | :--- | :--- | :---: |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` | No |
| `SERVER_PORT` | HTTP server port | `8080` | No |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://postgres-db:5432/knowledgenetwork_db` | No |
| `SPRING_DATASOURCE_USERNAME` | Database administrator username | `kn_admin` | Yes |
| `SPRING_DATASOURCE_PASSWORD` | Database administrator password | `*` | **YES** |
| `APP_JWT_SECRET` | HMAC SHA-256 secret (Min 64 hex chars) | `*` | **YES** |
| `APP_CORS_ALLOWED_ORIGINS` | Permitted production frontend origin(s) | `https://app.yourdomain.com` | No |
| `APP_COOKIE_SECURE` | Set `secure=true` on HttpOnly refresh token cookie | `true` | No |
| `SPRING_MAIL_HOST` | Outbound SMTP server hostname | `smtp.gmail.com` | No |
| `MAIL_USERNAME` | SMTP account email address | `noreply@yourdomain.com` | Yes |
| `MAIL_PASSWORD` | SMTP password / App password | `*` | **YES** |

---

## 4. Docker Deployment Workflow

### 1. Build Multi-Stage Production Container
```bash
docker build -t knowledgenetwork:latest .
```

### 2. Run Container with Environment Configuration
```bash
docker run -d \
  --name knowledgenetwork-app \
  -p 8080:8080 \
  --env-file production.env \
  knowledgenetwork:latest
```

---

## 5. Database Flyway Migrations & Clean-Start

Flyway executes automatically on application startup when `SPRING_FLYWAY_ENABLED=true`.

* **Baseline Schema**: `V1__init_schema.sql` creates base tables (`users`, `workspaces`, `nodes`, `edges`).
* **Migrations**: Executed sequentially from `V1` to `V15`.
* **Validation**: Schema state is validated (`ddl-auto: validate`) to ensure Java entities strictly match database column definitions without altering production tables dynamically.

---

## 6. Database Backup & Disaster Recovery Protocols

1. **Automated Database Snapshots**: Enable daily automated snapshot backups with a 30-day retention window on managed PostgreSQL services.
2. **Manual Pre-Deployment Backup**:
   ```bash
   pg_dump -U kn_admin -h postgres-host -d knowledgenetwork_db -F c -b -v -f kn_db_backup_$(date +%F).dump
   ```
3. **Restoration Verification**:
   ```bash
   pg_restore -U kn_admin -h postgres-host -d knowledgenetwork_db -v kn_db_backup.dump
   ```

---

## 7. Application Health Checks & Monitoring

* **Liveness Probe**: `GET http://localhost:8080/actuator/health/liveness` (Returns HTTP 200 `{"status": "UP"}`)
* **Readiness Probe**: `GET http://localhost:8080/actuator/health/readiness` (Verifies DB connectivity)
* **Prometheus Metrics**: Available at `/actuator/prometheus` for integration with Grafana dashboard visualizers.

---

## 8. Rollback Considerations

1. **Application Image Rollback**: In case of application binary issues, re-deploy the previous container tag (`knowledgenetwork:v1.0.0-previous`).
2. **Database Rollback**:
   * Standard Flyway migrations are backward-compatible.
   * If a migration modifies destructive schema elements, restore the pre-deployment database dump before rolling back application binaries.

---

## 9. Final Pre-Deployment Security Checklist

- [x] HTTPS TLS termination active on Reverse Proxy / Load Balancer.
- [x] `APP_JWT_SECRET` configured with 64+ character random hex string.
- [x] `APP_COOKIE_SECURE=true` enabled for HttpOnly refresh cookie protection.
- [x] `APP_CORS_ALLOWED_ORIGINS` explicitly configured for production domain.
- [x] Production database credentials injected strictly via environment variables.
- [x] Database automated backups and volume persistence enabled.
- [x] Actuator endpoints restricted to internal health/metrics scraping.
