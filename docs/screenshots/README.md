# Screenshots

Naming convention: `<order>-<topic>.png`

## Captured

### 01 — Architecture
High-level system diagram. Top row is the request path
(Client → Spring Boot → Redis); below is the observability path
(Spring Boot → Prometheus → Grafana). Spring Boot exposes two ports:
`:8080` for the HTTP API and `:9464` for the OpenTelemetry-format
`/metrics` endpoint that Prometheus scrapes every 15 s.

![Architecture](01-architecture.png)

### 02 — Dashboard at peak load
Grafana during Experiment 1 (peak throughput, sliding window).
100 users, c=50, 30s. Result: 21K req/s, p99 ~9 ms, 0% rejection.

![Dashboard at peak load](02-dashboard-peak.png)

### 03 — Dashboard during rejection-heavy run
Grafana during Experiment 4 (rejection accuracy).
Single user, c=50, 30s. Limiter cap 100 req per 10s.
Result: exactly 300 allowed (mathematically correct), 54,020 rejected.
Rejection ratio gauge pegged at 99.4%.

![Dashboard during rejection](03-dashboard-rejection.png)

### 04 — Scaling curve (5 humps)
Grafana during Experiment 2 (scaling curve).
Concurrency stepped through 10 → 50 → 100 → 200 → 400.
Five distinct request-rate humps; saturation knee visible at c=200.

![Scaling curve](04-scaling-curve.png)

### 05 — Strategy comparison
Grafana during Experiment 3 (algorithm comparison).
Two humps in "Request Rate by Strategy" panel: `fixed_window` (yellow)
peaks higher than `sliding_window` (green) under identical load.
Visualizes the +12% throughput / -42% p99 advantage of fixed window.

![Strategy comparison](05-strategy-comparison.png)