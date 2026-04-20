# Payment Service

## Overview

Payment Service quản lý thanh toán và hóa đơn cho quy trình thuê xe: tạo thanh toán, xử lý giao dịch, hoàn tiền và quản lý invoice.

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
| GET | `/api/payments/health` | Health check |
| POST | `/api/payments` | Create payment |
| GET | `/api/payments` | List payments |
| GET | `/api/payments/{paymentId}` | Get payment by ID |
| GET | `/api/payments/rental/{rentalId}` | List payments by rental |
| PATCH | `/api/payments/{paymentId}/process` | Process pending payment |
| POST | `/api/payments/{paymentId}/refund` | Create refund |
| POST | `/api/invoices` | Create invoice |
| GET | `/api/invoices` | List invoices |
| GET | `/api/invoices/{invoiceId}` | Get invoice by ID |
| GET | `/api/invoices/number/{invoiceNumber}` | Get invoice by invoice number |
| GET | `/api/invoices/rental/{rentalId}` | Get invoice by rental ID |
| PATCH | `/api/invoices/{invoiceId}/penalty` | Update penalty amount |
| PATCH | `/api/invoices/{invoiceId}/paid` | Mark invoice as paid |
| PATCH | `/api/invoices/{invoiceId}/refund` | Add invoice refund amount |

> Full API specification: [`docs/api-specs/payment-service.yaml`](../../docs/api-specs/payment-service.yaml)

## Running

```bash
# From project root
docker compose up payment-service --build
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | MySQL hostname | `payment-db` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `payment_db` |
| `DB_USER` | Database username | `root` |
| `DB_PASSWORD` | Database password | `password` |
| `RABBITMQ_HOST` | RabbitMQ hostname | `rabbitmq` |
| `RABBITMQ_PORT` | RabbitMQ port | `5672` |
| `RABBITMQ_USER` | RabbitMQ username | `guest` |
| `RABBITMQ_PASSWORD` | RabbitMQ password | `guest` |
