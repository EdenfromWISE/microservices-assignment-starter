# Implementation Plan

**Project:** Car Rental Management System — Microservices Assignment  
**Team:**
- **Nguyễn Quý Hạnh — B22DCCN277** (Người 1): rental-service + infrastructure
- **Nguyễn Anh Tuấn — B22DCCN757** (Người 2): payment-service + damage-penalty-service + frontend

**Source reference:** `../car-rental-system/` — toàn bộ code đã implement sẵn, copy và điều chỉnh theo hướng dẫn bên dưới.

---

## Tổng quan phân công

| Component | Người thực hiện | Độ phức tạp |
|-----------|----------------|-------------|
| rental-service | Hạnh (Người 1) | ⭐⭐⭐ Cao — State Pattern, orchestrator |
| docker-compose.yml | Hạnh (Người 1) | ⭐⭐ Trung bình |
| gateway (Nginx) | Hạnh (Người 1) | ⭐ Thấp |
| payment-service | Tuấn (Người 2) | ⭐⭐ Trung bình |
| damage-penalty-service | Tuấn (Người 2) | ⭐⭐ Trung bình |
| frontend | Tuấn (Người 2) | ⭐⭐ Trung bình |

---

## Người 1 — Nguyễn Quý Hạnh (B22DCCN277)

### 1. Chuẩn bị cấu trúc thư mục

**Việc cần làm:**
- Đổi tên `services/service-a/` → `services/rental-service/`
- Tạo thư mục `services/rental-service/src/`
- Copy toàn bộ nội dung từ `../car-rental-system/services/rental-service/src/` vào

```
services/rental-service/
├── src/
│   └── main/java/com/carrental/rental/
│       ├── config/          ← RabbitMQConfig.java, WebCorsConfig.java
│       ├── controller/      ← RentalController.java
│       ├── dto/             ← RentalCreateDTO.java, InspectionDTO.java
│       ├── event/           ← RentalEventPublisher.java
│       ├── exception/
│       ├── model/           ← Rental.java, RentalStatus.java, RentalState*.java
│       ├── repository/      ← RentalRepository.java
│       └── service/         ← RentalService.java
├── Dockerfile
├── pom.xml
└── src/main/resources/application.yml
```

### 2. Thêm endpoint GET /health

Thêm vào `RentalController.java`:

```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "ok"));
}
```

> ⚠️ Đây là endpoint bắt buộc — instructor sẽ kiểm tra đầu tiên.

### 3. Cập nhật application.yml

Sửa `src/main/resources/application.yml` để dùng biến môi trường Docker:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:rental_db}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQL8Dialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

### 4. Cập nhật pom.xml

Thay PostgreSQL driver bằng MySQL:

```xml
<!-- Xóa dependency này nếu có -->
<!-- <artifactId>postgresql</artifactId> -->

<!-- Thêm MySQL driver -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 5. Cập nhật Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 6. Cập nhật docker-compose.yml

Thay toàn bộ nội dung `docker-compose.yml` hiện tại:

```yaml
services:
  frontend:
    build: ./frontend
    ports:
      - "${FRONTEND_PORT:-3000}:3000"
    depends_on:
      - gateway
    networks:
      - app-network

  gateway:
    build: ./gateway
    ports:
      - "${GATEWAY_PORT:-8080}:80"
    depends_on:
      - rental-service
      - payment-service
      - damage-penalty-service
    networks:
      - app-network

  rental-service:
    build: ./services/rental-service
    ports:
      - "8081:8081"
    env_file: .env
    environment:
      DB_HOST: rental-db
      DB_PORT: 3306
      DB_NAME: rental_db
      DB_USER: ${DB_USER:-root}
      DB_PASSWORD: ${DB_PASSWORD:-password}
      RABBITMQ_HOST: rabbitmq
    depends_on:
      - rental-db
      - rabbitmq
    networks:
      - app-network

  payment-service:
    build: ./services/payment-service
    ports:
      - "8082:8082"
    env_file: .env
    environment:
      DB_HOST: payment-db
      DB_PORT: 3306
      DB_NAME: payment_db
      DB_USER: ${DB_USER:-root}
      DB_PASSWORD: ${DB_PASSWORD:-password}
      RABBITMQ_HOST: rabbitmq
    depends_on:
      - payment-db
      - rabbitmq
    networks:
      - app-network

  damage-penalty-service:
    build: ./services/damage-penalty-service
    ports:
      - "8083:8083"
    env_file: .env
    environment:
      DB_HOST: damage-db
      DB_PORT: 3306
      DB_NAME: damage_db
      DB_USER: ${DB_USER:-root}
      DB_PASSWORD: ${DB_PASSWORD:-password}
      RABBITMQ_HOST: rabbitmq
    depends_on:
      - damage-db
      - rabbitmq
    networks:
      - app-network

  rental-db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: rental_db
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-password}
    volumes:
      - rental-db-data:/var/lib/mysql
    networks:
      - app-network

  payment-db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: payment_db
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-password}
    volumes:
      - payment-db-data:/var/lib/mysql
    networks:
      - app-network

  damage-db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: damage_db
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD:-password}
    volumes:
      - damage-db-data:/var/lib/mysql
    networks:
      - app-network

  rabbitmq:
    image: rabbitmq:3-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    networks:
      - app-network

networks:
  app-network:
    driver: bridge

volumes:
  rental-db-data:
  payment-db-data:
  damage-db-data:
```

### 7. Cấu hình Gateway (Nginx)

Tạo file `gateway/src/nginx.conf`:

```nginx
events {}

http {
    upstream rental-service {
        server rental-service:8081;
    }
    upstream payment-service {
        server payment-service:8082;
    }
    upstream damage-penalty-service {
        server damage-penalty-service:8083;
    }

    server {
        listen 80;

        location /api/rentals {
            proxy_pass http://rental-service;
        }

        location /api/payments {
            proxy_pass http://payment-service;
        }

        location /api/invoices {
            proxy_pass http://payment-service;
        }

        location /api/damage-reports {
            proxy_pass http://damage-penalty-service;
        }

        location /api/penalties {
            proxy_pass http://damage-penalty-service;
        }

        location /health {
            return 200 '{"status":"ok"}';
            add_header Content-Type application/json;
        }
    }
}
```

Cập nhật `gateway/Dockerfile`:

```dockerfile
FROM nginx:alpine
COPY src/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

### 8. Cập nhật README.md

Điền thông tin team vào `README.md` theo yêu cầu của assignment.

### 9. Cập nhật docs/api-specs/

Đổi tên và điền nội dung:
- `docs/api-specs/service-a.yaml` → `docs/api-specs/rental-service.yaml`

Copy API spec từ `../car-rental-system/docs/API_DESIGN.md` phần Rental Service.

---

## Người 2 — Nguyễn Anh Tuấn (B22DCCN757)

### 1. Chuẩn bị payment-service

**Việc cần làm:**
- Đổi tên `services/service-b/` → `services/payment-service/`
- Copy toàn bộ từ `../car-rental-system/services/payment-service/src/`

```
services/payment-service/
├── src/main/java/com/carrental/payment/
│   ├── config/          ← RabbitMQConfig.java, WebCorsConfig.java
│   ├── controller/      ← PaymentController.java, InvoiceController.java
│   ├── dto/             ← PaymentCreateDTO.java, PaymentProcessDTO.java, InvoiceCreateDTO.java
│   ├── event/           ← PaymentEventPublisher.java
│   ├── exception/
│   ├── model/           ← Payment.java, Invoice.java, PaymentStatus.java, PaymentType.java, PaymentMethod.java
│   ├── repository/      ← PaymentRepository.java, InvoiceRepository.java
│   └── service/         ← PaymentService.java, InvoiceService.java
├── Dockerfile
├── pom.xml
└── src/main/resources/application.yml
```

### 2. Thêm endpoint GET /health — payment-service

Thêm vào `PaymentController.java`:

```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "ok"));
}
```

### 3. Cập nhật application.yml — payment-service

```yaml
server:
  port: 8082

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:payment_db}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQL8Dialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

### 4. Chuẩn bị damage-penalty-service

**Việc cần làm:**
- Tạo thư mục `services/damage-penalty-service/`
- Copy toàn bộ từ `../car-rental-system/services/damage-penalty-service/src/`

```
services/damage-penalty-service/
├── src/main/java/com/carrental/damagepenalty/
│   ├── config/          ← RabbitMQConfig.java, WebCorsConfig.java
│   ├── controller/      ← DamageController.java, PenaltyController.java
│   ├── dto/             ← DamageReportDTO.java, PenaltyDTO.java, PenaltyPaymentDTO.java
│   ├── event/           ← DamageEventPublisher.java, PenaltyEventPublisher.java
│   ├── exception/       ← DamageNotFoundException.java, PenaltyNotFoundException.java
│   ├── model/           ← DamageReport.java, Penalty.java + enums
│   ├── repository/      ← DamageRepository.java, PenaltyRepository.java
│   └── service/         ← DamageService.java, PenaltyService.java
├── Dockerfile
├── pom.xml
└── src/main/resources/application.yml
```

### 5. Thêm endpoint GET /health — damage-penalty-service

Thêm vào `DamageController.java`:

```java
@GetMapping("/health")
public ResponseEntity<Map<String, String>> health() {
    return ResponseEntity.ok(Map.of("status", "ok"));
}
```

### 6. Cập nhật application.yml — damage-penalty-service

```yaml
server:
  port: 8083

spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:damage_db}
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQL8Dialect
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
```

### 7. Cập nhật pom.xml — cả 2 service

Thay PostgreSQL driver bằng MySQL (giống Người 1 — Mục 4).

### 8. Cập nhật Dockerfile — payment-service

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 9. Cập nhật Dockerfile — damage-penalty-service

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 10. Frontend

**Source:** Copy từ `../car-rental-system/frontend-test/` — đã có 4 app vanilla JS hoàn chỉnh.

**Việc cần làm:**
- Copy nội dung các app vào `frontend/src/`
- Gộp 3 app (rental-app, payment-app, damage-app) thành 1 UI duy nhất
- Đổi tất cả URL từ `http://localhost:808x` → `http://localhost:8080` (qua Gateway)

**Cấu trúc gợi ý:**

```
frontend/src/
├── index.html       ← Navigation chính (tabs: Rental / Payment / Damage)
├── rental.html      ← Copy từ frontend-test/rental-app/index.html
├── payment.html     ← Copy từ frontend-test/payment-app/index.html
├── damage.html      ← Copy từ frontend-test/damage-app/index.html
├── rental.js        ← Copy từ frontend-test/rental-app/app.js, đổi base URL
├── payment.js       ← Copy từ frontend-test/payment-app/app.js, đổi base URL
├── damage.js        ← Copy từ frontend-test/damage-app/app.js, đổi base URL
└── style.css        ← Copy từ frontend-test/rental-app/style.css
```

**Đổi base URL trong mỗi file JS:**
```javascript
// Trước
const BASE_URL = 'http://localhost:8081';
// Sau
const BASE_URL = 'http://localhost:8080';  // qua Gateway
```

**Dockerfile frontend:**

```dockerfile
FROM nginx:alpine
COPY src/ /usr/share/nginx/html/
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
```

Thêm `nginx.conf` cho frontend:

```nginx
server {
    listen 3000;
    root /usr/share/nginx/html;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

### 11. Cập nhật docs/api-specs/

Đổi tên và điền nội dung:
- `docs/api-specs/service-b.yaml` → `docs/api-specs/payment-service.yaml`
- Tạo thêm `docs/api-specs/damage-penalty-service.yaml`

Copy API spec từ `../car-rental-system/docs/API_DESIGN.md` phần Payment Service và Damage-Penalty Service.

---

## Checklist tích hợp (cả 2 làm cùng)

Sau khi mỗi người hoàn thành phần của mình, kiểm tra tích hợp:

```bash
# Test cold start
docker compose down --volumes --remove-orphans
docker compose up --build

# Kiểm tra health
curl http://localhost:8080/health
curl http://localhost:8080/api/rentals/health
curl http://localhost:8080/api/payments/health
curl http://localhost:8080/api/damage-reports/health

# Test luồng cơ bản
# 1. Tạo rental
curl -X POST http://localhost:8080/api/rentals \
  -H "Content-Type: application/json" \
  -d '{"customerId":"cust-001","vehicleId":"veh-001","startDate":"2026-05-01T09:00:00","endDate":"2026-05-05T18:00:00","pickupLocation":"Hanoi","returnLocation":"Hanoi","dailyRate":500000}'

# 2. Mở frontend
# http://localhost:3000
```

---

## Lưu ý chung

| # | Lưu ý |
|---|-------|
| 1 | **Không dùng `localhost`** trong code — dùng service name (e.g., `rental-service`, `payment-service`) |
| 2 | **Không hardcode password** — dùng biến môi trường từ `.env` |
| 3 | Mỗi service phải có `GET /health` → `{"status": "ok"}` |
| 4 | Chạy `docker compose up --build` sau mỗi thay đổi lớn để kiểm tra sớm |
| 5 | Commit thường xuyên để instructor thấy tiến độ |
| 6 | Database dùng **MySQL 8** (không phải PostgreSQL) — đã migrate |
