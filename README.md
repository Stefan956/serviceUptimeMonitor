# Service Uptime Monitor

![CI](https://github.com/Stefan956/serviceUptimeMonitor/actions/workflows/ci.yml/badge.svg)

A microservices application that health-checks registered HTTP services, fires email alerts when a service goes down, and streams live status data to a dashboard — all observable through Grafana.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 · Go 1.24 |
| Framework | Spring Boot 4.0.1 |
| HTTP client | Spring WebFlux (`WebClient`) |
| Persistence | Spring Data JPA · PostgreSQL (prod) · H2 (dev/test) |
| Scheduling | Spring `@Scheduled` |
| Alerting | Spring Mail (JavaMailSender) |
| Real-time | Server-Sent Events (`SseEmitter`) |
| Metrics | Micrometer · Prometheus |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 · Mockito · Spring Boot Test |
| Containers | Docker · Docker Compose |
| Observability | Prometheus · Grafana (pre-provisioned dashboard) |

---

## Architecture

```
 ┌───────────────────┐
 │  Demo Service     │  simulates HEALTHY → DEGRADED → DOWN → RECOVERING cycle
 │  (Go)             │
 └───────────────────┘
         │  health checks (HTTP GET every 30 s)
         ▼
 ┌───────────────────┐        POST /api/alerts/status-change
 │  Monitoring       │ ─────────────────────────────────────▶  ┌─────────────────┐
 │  Service  :8080   │                                         │  Alert Service  │
 │                   │ ◀── REST (current statuses, history) ── │  :8082          │
 └───────────────────┘                                         └─────────────────┘
         │  REST polling every 30 s                                      │
         ▼                                                               │ email
 ┌───────────────────┐                                         ┌─────────────────┐
 │  Dashboard        │ ──── SSE stream ────▶  Browser          │  MailHog        │
 │  Service  :8083   │                                         │  :8025 (UI)     │
 └───────────────────┘                                         └─────────────────┘

         │  /actuator/prometheus
         ▼
 ┌───────────────────┐   scrape    ┌─────────────┐   query   ┌──────────────┐
 │  Monitoring       │ ──────────▶ │  Prometheus │ ────────▶ │   Grafana    │
 │  Service  :8080   │             │  :9090      │           │   :3000      │
 └───────────────────┘             └─────────────┘           └──────────────┘

 ┌─────────────────────────────────────────────────────────┐
 │  PostgreSQL :5432                                       │
 │  uptime_monitor db  (Monitoring Service)                │
 │  alert_db           (Alert Service)                     │
 └─────────────────────────────────────────────────────────┘
```

| Service | Responsibility |
|---|---|
| **Monitoring Service** | Registers services, runs health checks, persists results, sends status-change events to the Alert Service, exposes Prometheus metrics |
| **Alert Service** | Receives status-change events, sends email notifications, stores alert history |
| **Dashboard Service** | Aggregates data from the Monitoring Service, exposes a REST overview and a real-time SSE stream |
| **Demo Service** | Simulates a monitored service — cycles through HEALTHY, DEGRADED (slow responses), DOWN (503), and RECOVERING states automatically on a timer, generating realistic alerts and status history without manual intervention |

---

## Getting Started

```bash
git clone https://github.com/Stefan956/serviceUptimeMonitor.git
cd serviceUptimeMonitor
docker compose up --build
```

The first build downloads Maven dependencies and compiles all services — this takes a few minutes. Subsequent starts are fast.

Six monitored services are seeded automatically on first startup (no manual registration needed). The Demo Service begins cycling through health states immediately, generating alerts and status history within the first few minutes.

### Service URLs

| Service | URL |
|---|---|
| Monitoring Service — Swagger UI | http://localhost:8080/swagger-ui.html |
| Alert Service — Swagger UI | http://localhost:8082/swagger-ui.html |
| Dashboard Service — Swagger UI | http://localhost:8083/swagger-ui.html |
| Grafana | http://localhost:3000 &nbsp;·&nbsp; `admin` / `admin` |
| Prometheus | http://localhost:9090 |
| MailHog (captured alert emails) | http://localhost:8025 |

### Run a single service locally (no Docker)

```bash
cd monitoring-service && mvn spring-boot:run
```

Each service starts with an in-memory H2 database and hot-reload via Spring DevTools.

---

## Quick Demo

### 1 — Check the current status of all services

```bash
curl http://localhost:8080/api/monitoring/read/current-statuses
```

### 2 — Subscribe to real-time status updates (SSE)

```bash
curl -N http://localhost:8083/api/dashboard/stream
```

### 3 — View alert history

```bash
curl http://localhost:8082/api/alerts
```

---

## API Reference

Full interactive docs are available via Swagger UI when the services are running.

### Monitoring Service — `localhost:8080`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/monitoring/services` | Register a new service |
| `GET` | `/api/monitoring/services` | List all registered services |
| `GET` | `/api/monitoring/services/{id}` | Get a service by ID |
| `PUT` | `/api/monitoring/services/{id}` | Update service configuration |
| `PATCH` | `/api/monitoring/services/{id}/enable` | Resume health checks |
| `PATCH` | `/api/monitoring/services/{id}/disable` | Pause health checks |
| `DELETE` | `/api/monitoring/services/{id}` | Remove a service and its history |
| `GET` | `/api/monitoring/read/current-statuses` | Latest check serviceHealthStatusResult per service |
| `GET` | `/api/monitoring/read/history/{serviceId}` | Full check history for a service |
| `GET` | `/actuator/prometheus` | Prometheus metrics |

### Alert Service — `localhost:8082`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/alerts/status-change` | Receive a status-change event (called by Monitoring Service) |
| `GET` | `/api/alerts` | Full alert history |
| `GET` | `/api/alerts/service/{serviceName}` | Alert history for one service |

### Dashboard Service — `localhost:8083`

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/dashboard/overview` | UP/DOWN counts + full status list |
| `GET` | `/api/dashboard/statuses` | Current status of all services |
| `GET` | `/api/dashboard/services` | All registered services |
| `GET` | `/api/dashboard/history/{serviceId}` | Check history for one service |
| `GET` | `/api/dashboard/stream` | SSE stream — real-time updates every 30 s |

---

## Observability

The Monitoring Service exposes Prometheus metrics at `/actuator/prometheus`. Grafana starts pre-configured with both a Prometheus datasource and a provisioned **Service Uptime Monitor** dashboard — log in at `http://localhost:3000` (`admin` / `admin`) to view it immediately. The dashboard includes 9 panels covering HTTP request rate, response time, JVM heap memory, DB connection pool, and Tomcat thread pool.

---

## Design Notes

### SSE stream is polling-based

`DashboardSseService` polls the Monitoring Service every 30 s and pushes the serviceHealthStatusResult to subscribers. Clients therefore see updates with up to 30 s latency, and the dashboard service makes one HTTP call per interval regardless of how many clients are connected.

A production alternative would be to publish status-change events to a message broker (Kafka or RabbitMQ). The Dashboard Service would consume those events and push them immediately — zero polling overhead, instant fan-out to all SSE clients.

---

## Testing

```bash
# Run all tests across all three services
mvn test

# Run tests for a single service
cd monitoring-service && mvn test
```

| Service | Tests |
|---|---|
| Monitoring Service | 76 |
| Alert Service | 34 |
| Dashboard Service | 35 |

---

## Project Structure

```
serviceUptimeMonitor/
├── monitoring-service/
│   ├── src/main/java/
│   │   ├── client/                  # AlertServiceClient (WebClient)
│   │   ├── configuration/           # WebClientConfig
│   │   ├── controller/              # MonitoredServiceController, MonitoringReadController
│   │   ├── enums/                   # ServiceHealthStatus
│   │   ├── exception/               # GlobalExceptionHandler
│   │   ├── mapper/                  # Entity ↔ DTO mapping
│   │   ├── persistence/
│   │   │   ├── entity/              # JPA entities (BaseEntity, MonitoredService, ServiceStatus)
│   │   │   └── repository/          # Spring Data repositories (DAOs)
│   │   ├── requests/
│   │   │   ├── event/               # ServiceStatusChangeEvent
│   │   │   ├── request/             # Inbound request DTOs
│   │   │   └── response/            # Outbound response DTOs
│   │   ├── scheduler/               # MonitoringScheduler (@Scheduled)
│   │   ├── service/                 # MonitoringService, MonitoredServiceManagementService, ...
│   │   └── util/                    # Constants
│   └── src/test/
├── alert-service/                   # same layout as monitoring-service
│   └── src/main/java/
│       ├── controller/              # AlertController
│       ├── enums/                   # NotificationChannel, ServiceHealthStatus
│       ├── exception/               # GlobalExceptionHandler, AlertProcessingException
│       ├── persistence/
│       │   ├── entity/              # Alert entity
│       │   └── repository/          # AlertRepository
│       ├── requests/
│       │   ├── request/             # AlertRequestDto
│       │   └── response/            # AlertResponseDto
│       └── service/                 # AlertProcessorService, EmailAlertService, ...
├── dashboard-service/               # read-only client — no persistence layer
│   └── src/main/java/
│       ├── client/                  # MonitoringServiceClient (WebClient)
│       ├── configuration/           # WebClientConfig
│       ├── controller/              # DashboardController
│       ├── enums/                   # ServiceHealthStatus
│       ├── exception/               # GlobalExceptionHandler
│       ├── requests/
│       │   └── response/            # Dashboard response DTOs
│       ├── service/                 # DashboardService, DashboardSseService
│       └── util/                    # Constants
├── demo-service/          # Go service simulating service lifecycle (HEALTHY → DEGRADED → DOWN → RECOVERING)
│   ├── main.go
│   ├── go.mod
│   └── Dockerfile
├── docker/
│   ├── grafana/           # Grafana datasource + pre-provisioned dashboard
│   ├── postgres/          # DB init script (creates alert_db)
│   └── prometheus/        # Prometheus scrape config
├── docker-compose.yml
└── README.md
```
