# API Gateway

## Overview

Gateway dùng Nginx làm entrypoint duy nhất cho frontend và route request đến các service nội bộ trong Docker network.

## Routing Table

| External Path | Target Service | Internal URL |
|---------------|----------------|--------------|
| `/health` | Gateway | static `{"status":"ok"}` |
| `/api/rentals/*` | rental-service | `http://rental-service:8081` |
| `/api/payments/*` | payment-service | `http://payment-service:8082` |
| `/api/invoices/*` | payment-service | `http://payment-service:8082` |
| `/api/damage-reports/*` | damage-penalty-service | `http://damage-penalty-service:8083` |
| `/api/penalties/*` | damage-penalty-service | `http://damage-penalty-service:8083` |

## Running

```bash
docker compose up gateway --build
```

## Notes

- Gateway listen nội bộ tại port `80`, publish ra host `8080` qua `docker-compose.yml`
- Chỉ dùng Compose service names cho upstream, không dùng `localhost`
