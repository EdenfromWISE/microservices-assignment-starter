# Damage & Penalty Service

## Overview

Damage & Penalty Service quản lý báo cáo hư hại và tiền phạt: ghi nhận hư hại, cập nhật trạng thái sửa chữa, tạo penalty và xử lý thanh toán penalty.

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Database | MySQL 8 |
| Messaging | RabbitMQ |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/damage-reports/health` | Health check |
| POST | `/api/damage-reports` | Create damage report |
| GET | `/api/damage-reports` | List damage reports |
| GET | `/api/damage-reports/{damageId}` | Get damage report by ID |
| PATCH | `/api/damage-reports/{damageId}` | Update damage status/repair cost |
| POST | `/api/penalties` | Create penalty |
| GET | `/api/penalties` | List penalties |
| GET | `/api/penalties/{penaltyId}` | Get penalty by ID |
| POST | `/api/penalties/{penaltyId}/pay` | Pay penalty |

> Full API specification: [`docs/api-specs/damage-penalty-service.yaml`](../../docs/api-specs/damage-penalty-service.yaml)

## Running

```bash
# From project root
docker compose up damage-penalty-service --build
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MySQL hostname | `damage-db` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `damage_db` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USER` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
