# Analysis and Design — Step-by-Step Action Approach

> **Goal**: Analyze a specific business process and design a service-oriented automation solution (SOA/Microservices).
> **Scope**: 4–6 week assignment — focus on **one business process**, not an entire system.
>
> **Alternative to**: [`analysis-and-design-ddd.md`](analysis-and-design-ddd.md) (Domain-Driven Design approach).
> Choose **one** approach, not both. Use this if your team prefers discovering service boundaries through **decomposing concrete actions** rather than domain modeling.

**References:**
1. *Service-Oriented Architecture: Analysis and Design for Services and Microservices* — Thomas Erl (2nd Edition)
2. *Microservices Patterns: With Examples in Java* — Chris Richardson
3. *Bài tập — Phát triển phần mềm hướng dịch vụ* — Hung Dang (available in Vietnamese)

---

### How Step-by-Step Action differs from DDD

| | Step-by-Step Action (this document) | DDD |
|---|---|---|
| **Thinking direction** | Bottom-up: actions → group → service | Dual: top-down framing + bottom-up Event Storming |
| **Service boundary decided by** | Similarity of actions/functions | Semantic boundary of business domain |
| **Best suited for** | Small–medium systems, clearer technical scope | Complex business logic, multiple subdomains |
| **Key risk** | Services may be fragmented by technical logic | Requires deep domain understanding upfront |

Both approaches lead to a list of services with clear responsibilities. This approach is more structured and mechanical — useful when your team understands *what the system does* better than *what the business domain is*.

### Progression Overview

| Step | What you do | Output |
|------|------------|--------|
| **1.1** | Define the Business Process | Process diagram, actors, scope |
| **1.2** | Survey existing systems | System inventory |
| **1.3** | State non-functional requirements | NFR table |
| **2.1–2.2** | Decompose process & filter unsuitable actions | Filtered action list |
| **2.3** | Group reusable actions → Entity Service Candidates | Entity service table |
| **2.4** | Group process-specific actions → Task Service Candidate | Task service table |
| **2.5** | Map entities to REST Resources | Resource URI table |
| **2.6** | Associate capabilities with resources and HTTP methods | **Service capabilities → API endpoints** |
| **2.7** | Identify cross-cutting / high-autonomy candidates | Utility / Microservice Candidates |
| **2.8** | Show how services collaborate | Service composition diagram |
| **3.1** | Specify service contracts | OpenAPI endpoint tables |
| **3.2** | Design internal service logic | Flowchart per service |

---

## Part 1 — Analysis Preparation

### 1.1 Business Process Definition

- **Domain**: Car Rental Management
- **Business Process**: "Customer rents a car and returns it with damage found during inspection"
- **Actors**: Customer, Staff, Payment Gateway
- **Scope**: From booking creation to rental completion — including damage reporting and penalty payment. Excludes vehicle maintenance after penalty is paid.

**Process Diagram:**

![Car Rental Process](asset/Car%20Rental%20Process.jpg)

> 💡 **Tip:** A good scope for this assignment is a process with 5–15 steps and 2–4 actors. If your process has more than 20 steps, narrow the scope.

### 1.2 Existing Automation Systems

List existing systems, databases, or legacy logic related to this process.

| System Name | Type | Current Role | Interaction Method |
|-------------|------|--------------|-------------------|
| rental-service | Microservice | Manages rental lifecycle (booking → pickup → return → inspection → completion) | REST API |
| payment-service | Microservice | Processes deposit and penalty payments, generates invoices | REST API + RabbitMQ events |
| damage-penalty-service | Microservice | Records damage reports, calculates repair costs, manages penalties | REST API + RabbitMQ events |
| statistics-service | Microservice | Aggregates revenue and rental statistics, cached via Redis | REST API + RabbitMQ events |
| PostgreSQL | Database | Persistent storage for each service (database-per-service pattern) | JDBC / Spring Data JPA |
| RabbitMQ | Message Broker | Async event bus between services (e.g., penalty.created, payment.completed) | AMQP |
| Redis | Cache | Caches statistics query results (TTL: 1 hour) | Spring Cache / Lettuce |

### 1.3 Non-Functional Requirements

Non-functional requirements serve as input in two places:
- **2.7** — justifying Utility Service and Microservice Candidates
- **`docs/architecture.md` Section 1** — justifying architectural pattern choices (e.g., high availability → Circuit Breaker, scalability → Database per Service)

| Requirement    | Description |
|----------------|-------------|
| Performance    | Statistics queries must respond within 1 second — achieved via Redis caching (1-hour TTL) on the statistics-service |
| Security       | Role-based access control (RBAC): Customer can only access own rental data; Staff can manage rentals and damage reports; Admin has full access. All APIs secured via JWT |
| Scalability    | Rental and payment services must handle traffic spikes independently — each service scales horizontally; database-per-service pattern prevents shared bottlenecks |
| Availability   | Payment and rental services are critical — deployed with multiple replicas; RabbitMQ decouples services so a statistics or damage service outage does not block the core rental flow |

---

## Part 2 — REST/Microservices Modeling

### 2.1 Decompose Business Process & 2.2 Filter Unsuitable Actions

Decompose the process from 1.1 into granular actions. Mark actions unsuitable for service encapsulation.

> 💡 **How to do it:** Walk through your process diagram step by step. For each step, write one or more actions the system needs to perform. Then ask: *"Can this action be encapsulated as a reusable service call?"* If it requires irreducible human judgment or is a one-time manual task, mark it ❌.

| # | Action | Actor | Description | Suitable? |
|---|--------|-------|-------------|-----------|
| 1 | CreateBooking | Customer | Customer submits booking with vehicle ID, dates, pickup/return location, and daily rate | ✅ |
| 2 | PayDeposit | Customer | Customer initiates a deposit payment for the booking | ✅ |
| 3 | ProcessDepositPayment | Payment Gateway | External gateway processes the deposit transaction and returns success/failure | ✅ |
| 4 | ConfirmBooking | Staff | Staff reviews and confirms the booking after deposit is received — requires human judgment to verify customer identity and vehicle availability | ❌ |
| 5 | PickupVehicle | Customer | Customer arrives and picks up the vehicle; rental state transitions to IN_PROGRESS | ✅ |
| 6 | ReturnVehicle | Customer | Customer returns the vehicle; rental state transitions to INSPECTION | ✅ |
| 7 | InspectVehicle | Staff | Staff physically examines the vehicle for damage — requires irreducible human judgment | ❌ |
| 8 | ReportDamage | Staff | Staff submits a damage report with type, severity, and description | ✅ |
| 9 | CalculateRepairCostAndCreatePenalty | System | System automatically calculates repair cost based on damage severity and creates a penalty record | ✅ |
| 10 | NotifyCustomer | System | System sends penalty notice to customer with amount and due date | ✅ |
| 11 | PayPenalty | Customer | Customer initiates payment for the penalty amount | ✅ |
| 12 | ProcessPenaltyPayment | Payment Gateway | External gateway processes the penalty transaction and returns success/failure | ✅ |
| 13 | CompleteRental | System | System transitions rental to COMPLETED and updates statistics | ✅ |

### 2.3 Entity Service Candidates

Identify business entities and group reusable (agnostic) actions into Entity Service Candidates.

> 💡 **How to do it:** Look at the ✅ actions from 2.1–2.2. Ask: *"Which business entity does this action primarily read or modify?"* Actions that operate on the same entity are grouped together. Each group becomes an **Entity Service Candidate**.
>
> An action is **agnostic** (entity-level) if it is potentially reusable across multiple business processes — e.g., "GetCustomer" could be called from order, support, and billing processes.

| Entity | Service Candidate | Agnostic Actions |
|--------|-------------------|------------------|
| Rental | rental-service | CreateBooking, PickupVehicle, ReturnVehicle, CompleteRental |
| Payment | payment-service | PayDeposit, ProcessDepositPayment, PayPenalty, ProcessPenaltyPayment |
| DamageReport, Penalty | damage-penalty-service | ReportDamage, CalculateRepairCostAndCreatePenalty |

### 2.4 Task Service Candidate

Group process-specific (non-agnostic) actions into a Task Service Candidate.

> 💡 **How to do it:** From the ✅ actions in 2.1–2.2, find the ones that are **specific to this business process** and orchestrate multiple entities — they are not reusable on their own. These belong in a Task Service, which acts as the process orchestrator.

| Non-agnostic Action | Task Service Candidate |
|---------------------|------------------------|
| CompleteRental | rental-service (orchestrator) — coordinates with damage-penalty-service (check penalty status) and payment-service (verify all payments) before transitioning rental to COMPLETED |
| NotifyCustomer | rental-service (orchestrator) — triggers notification after penalty is created by damage-penalty-service |

> 💡 **Rule of thumb:** A Task Service typically calls two or more Entity Services in sequence to complete one business process step. If an action only touches one entity, it likely belongs in an Entity Service instead.

### 2.5 Identify Resources

Map entities/processes to REST URI Resources.

> 💡 **How to do it:** For each Entity Service from 2.3, define the primary REST resource URI. Resources are plural nouns, not verbs. The URI represents a collection or a single item in that collection.

| Entity / Process | Resource URI |
|------------------|--------------|
| Rental | `/rentals`, `/rentals/{id}` |
| Rental state transitions | `/rentals/{id}/confirm`, `/rentals/{id}/pickup`, `/rentals/{id}/return`, `/rentals/{id}/complete` |
| Payment | `/payments`, `/payments/{id}` |
| Invoice | `/invoices`, `/invoices/{id}` |
| DamageReport | `/damage-reports`, `/damage-reports/{id}` |
| Penalty | `/penalties`, `/penalties/{id}`, `/penalties/{id}/pay` |

> ⚠️ **Common mistake:** Using verbs in URIs (e.g., `/createOrder`). REST resources are nouns — the HTTP method (GET/POST/PUT/DELETE) expresses the action.

### 2.6 Associate Capabilities with Resources and Methods

> 💡 **How to do it:** For each service capability (action from 2.3–2.4), map it to a resource URI from 2.5 and the appropriate HTTP method. This table directly produces your API endpoint list for Part 3.

| Service Candidate | Capability | Resource | HTTP Method |
|-------------------|------------|----------|-------------|
| rental-service | CreateBooking | `/rentals` | POST |
| rental-service | PickupVehicle | `/rentals/{id}/pickup` | POST |
| rental-service | ReturnVehicle | `/rentals/{id}/return` | POST |
| rental-service | CompleteRental | `/rentals/{id}/complete` | POST |
| payment-service | PayDeposit | `/payments` | POST |
| payment-service | ProcessDepositPayment | `/payments/{id}/process` | POST |
| payment-service | PayPenalty | `/payments` | POST |
| payment-service | ProcessPenaltyPayment | `/payments/{id}/process` | POST |
| damage-penalty-service | ReportDamage | `/damage-reports` | POST |
| damage-penalty-service | CalculateRepairCostAndCreatePenalty | `/penalties` | POST (auto-triggered) |
| damage-penalty-service | NotifyCustomer | `/penalties/{id}` | GET (customer polls) |

> ⚠️ **Check:** Every ✅ action from 2.1–2.2 should appear in this table as a Capability. If an action is missing, trace back to 2.3–2.5 and add it.

### 2.7 Utility Service & Microservice Candidates

Based on Non-Functional Requirements (1.3) and Processing Requirements, identify cross-cutting utility logic or logic requiring high autonomy/performance.

> 💡 **How to do it:** Look at your NFRs from 1.3. Ask:
> - *"Is there a concern (e.g., authentication, logging, notifications) that appears across multiple services?"* → **Utility Service**
> - *"Is there a capability that must scale independently or tolerate failure in isolation?"* → **Microservice Candidate** (extract from Entity/Task service)

| Candidate | Type (Utility / Microservice) | Justification (link to NFR or process requirement) |
|-----------|-------------------------------|-----------------------------------------------------|
| payment-service | Microservice | NFR: Availability + Security — payment processing is critical and must be isolated so that a rental-service outage cannot block payment transactions; also isolates financial logic for security |

### 2.8 Service Composition Candidates

Interaction diagram showing how Service Candidates collaborate to fulfill the business process.

> 💡 **How to do it:** Walk through the business process from 1.1 again. For each step, identify which service handles it and what inter-service calls are made. The Task Service (2.4) is typically the orchestrator in the center of the diagram.

![Service Composition Diagram](asset/Rental%20Service%20Payment-serviceDiagram.png)

> ⚠️ **Check:** Compare this diagram with your process diagram in 1.1. Every step in the business process should be handled by at least one service. If a step is not covered, you may be missing a service candidate.

---

## Part 3 — Service-Oriented Design

> Part 3 is the **convergence point** — regardless of whether you used Step-by-Step Action or DDD in Part 2, the outputs here are the same: service contracts and service logic.

### 3.1 Uniform Contract Design

Service Contract specification for each service. Full OpenAPI specs:
- [`docs/api-specs/rental-service.yaml`](api-specs/rental-service.yaml)
- [`docs/api-specs/payment-service.yaml`](api-specs/payment-service.yaml)
- [`docs/api-specs/damage-penalty-service.yaml`](api-specs/damage-penalty-service.yaml)
- [`docs/api-specs/statistics-service.yaml`](api-specs/statistics-service.yaml)

**rental-service:**

| Endpoint | Method | Description | Request Body | Response Codes |
|----------|--------|-------------|--------------|----------------|
| `/rentals` | POST | Create a new booking | `{ customerId, vehicleId, startDate, endDate, pickupLocation, returnLocation, dailyRate }` | 201, 400 |
| `/rentals/{id}` | GET | Get rental details | — | 200, 404 |
| `/rentals/{id}/pickup` | POST | Transition rental to IN_PROGRESS | — | 200, 409 |
| `/rentals/{id}/return` | POST | Transition rental to INSPECTION | — | 200, 409 |
| `/rentals/{id}/complete` | POST | Transition rental to COMPLETED | — | 200, 409 |

**payment-service:**

| Endpoint | Method | Description | Request Body | Response Codes |
|----------|--------|-------------|--------------|----------------|
| `/payments` | POST | Create and process a payment (deposit or penalty) | `{ rentalId, type, amount, method }` | 201, 400 |
| `/payments/{id}` | GET | Get payment details | — | 200, 404 |
| `/payments/{id}/process` | POST | Process pending payment via gateway | — | 200, 402 |
| `/invoices/{id}` | GET | Get invoice for a payment | — | 200, 404 |

**damage-penalty-service:**

| Endpoint | Method | Description | Request Body | Response Codes |
|----------|--------|-------------|--------------|----------------|
| `/damage-reports` | POST | Submit a damage report for a rental | `{ rentalId, vehicleId, damageType, severity, description }` | 201, 400 |
| `/damage-reports/{id}` | GET | Get damage report details | — | 200, 404 |
| `/penalties` | GET | List penalties for a rental | `?rentalId={id}` | 200 |
| `/penalties/{id}` | GET | Get penalty details | — | 200, 404 |
| `/penalties/{id}/pay` | POST | Mark penalty as paid | `{ paymentId }` | 200, 409 |

**statistics-service:**

| Endpoint | Method | Description | Request Body | Response Codes |
|----------|--------|-------------|--------------|----------------|
| `/statistics/revenue/monthly/{year}/{month}` | GET | Get monthly revenue | — | 200 |
| `/statistics/revenue/quarterly/{year}/{quarter}` | GET | Get quarterly revenue | — | 200 |
| `/statistics/revenue/yearly/{year}` | GET | Get yearly revenue | — | 200 |

### 3.2 Service Logic Design

Internal processing flow for each service.

> 💡 **How to do it:** For each service, pick its most important endpoint and draw the internal logic. Focus on: input validation → business rule checks → persistence/external calls → response.

**rental-service — POST /rentals (CreateBooking):**

```mermaid
flowchart TD
    A[Receive POST /rentals] --> B{Validate input?\ncustomerId, vehicleId, dates, dailyRate}
    B -->|Invalid| C[Return 400 Bad Request]
    B -->|Valid| D{Vehicle available?}
    D -->|No| E[Return 409 Conflict]
    D -->|Yes| F[(Save Rental — state: PENDING)]
    F --> G[Publish event: rental.created]
    G --> H[Return 201 Rental PENDING]
```

**damage-penalty-service — POST /damage-reports (ReportDamage):**

```mermaid
flowchart TD
    A[Receive POST /damage-reports] --> B{Validate input?\nrentalId, vehicleId, damageType, severity}
    B -->|Invalid| C[Return 400 Bad Request]
    B -->|Valid| D{Rental in INSPECTION state?}
    D -->|No| E[Return 409 Conflict]
    D -->|Yes| F[(Save DamageReport — status: REPORTED)]
    F --> G{Severity MODERATE or MAJOR?}
    G -->|No| H[Return 201 DamageReport REPORTED]
    G -->|Yes| I[Calculate repair cost]
    I --> J[(Create Penalty — status: UNPAID)]
    J --> K[Publish event: penalty.created]
    K --> H
```

**payment-service — POST /payments (PayDeposit / PayPenalty):**

```mermaid
flowchart TD
    A[Receive POST /payments] --> B{Validate input?\nrentalId, type, amount, method}
    B -->|Invalid| C[Return 400 Bad Request]
    B -->|Valid| D[(Save Payment — status: PENDING)]
    D --> E[Call Payment Gateway]
    E --> F{Gateway response?}
    F -->|Success| G[(Update Payment — status: COMPLETED)]
    G --> H[Publish event: payment.completed]
    H --> I[Return 201 Payment COMPLETED]
    F -->|Failed| J[(Update Payment — status: FAILED)]
    J --> K[Return 402 Payment Required]
```
