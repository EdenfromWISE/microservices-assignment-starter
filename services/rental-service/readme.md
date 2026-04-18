# Rental Service

## Overview

Rental Service quản lý vòng đời đơn thuê xe (booking -> confirm -> pickup -> return -> inspection -> complete/cancel) theo State Pattern.

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
| GET | `/api/rentals/health` | Health check |
| POST | `/api/rentals` | Create rental |
| GET | `/api/rentals` | List rentals |
| GET | `/api/rentals/{rentalId}` | Get rental by ID |
| PATCH | `/api/rentals/{rentalId}/confirm` | Confirm rental |
| PATCH | `/api/rentals/{rentalId}/pickup` | Mark pickup |
| PATCH | `/api/rentals/{rentalId}/return` | Mark return |
| PATCH | `/api/rentals/{rentalId}/inspection` | Complete inspection |
| PATCH | `/api/rentals/{rentalId}/complete` | Complete rental |
| PATCH | `/api/rentals/{rentalId}/cancel` | Cancel rental |
| PATCH | `/api/rentals/{rentalId}/penalty` | Update penalty amount |
| GET | `/api/rentals/{rentalId}/history` | Get state history |

> Full API specification: [`docs/api-specs/rental-service.yaml`](../../docs/api-specs/rental-service.yaml)

## Running

```bash
# From project root
docker compose up rental-service --build
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MySQL hostname | `rental-db` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `rental_db` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
