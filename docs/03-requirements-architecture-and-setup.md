# Stage 3: Requirements, Architecture and Technology Setup

| Metadata | Details |
| :--- | :--- |
| **Project Name** | Containerized E-Notary Appointment System |
| **Coursework Reference** | `docs/devops-project-guidelines.ods` (Row 39) |
| **Student** | Aryan Racha (Roll No: `23102C0055`) |
| **Deliverables Covered** | SRS Summary, Use-Case Diagram, Architecture Diagram, Data Model / API List, Working Local Setup |

---

## 1. Software Requirements Specification (SRS) Summary

### 1.1 Functional Requirements (FR)

- **FR-01 (User Registration & Authentication):** The system shall allow Clients and Notaries to register with full name, email, phone number, and password. Passwords must be hashed using BCrypt. Roles (`CLIENT`, `NOTARY`, `ADMIN`) must restrict access to role-specific dashboards.
- **FR-02 (Availability & Slot Configuration):** The system shall allow Notaries to define their available hours and automatically generate discrete 30-minute time slots for specified dates.
- **FR-03 (Slot Browsing & Filtering):** The system shall allow Clients to view available slots filtered by Notary and Date. Slots already booked or locked must not be selectable.
- **FR-04 (Appointment Booking & Reference Generation):** The system shall allow authenticated Clients to reserve an available slot, specify the document type (e.g., *Affidavit*, *Power of Attorney*, *Property Deed*), and attach notes. Upon submission, an alphanumeric reference code (`ENOT-YYYY-XXXX`) is assigned.
- **FR-05 (Booking Confirmation Workflow):** The system shall provide Notaries with a dashboard to review incoming appointment requests and transition them from `REQUESTED` to `CONFIRMED` or `REJECTED`.
- **FR-06 (Appointment Cancellation & Automatic Slot Reactivation):** The system shall permit either party to cancel a scheduled appointment with a mandatory cancellation reason. Cancellation automatically reverts the associated slot back to `AVAILABLE`.
- **FR-07 (Public Status Tracking):** The system shall provide an unauthenticated public tracking page where users can input their booking reference code to view current appointment status and details.

### 1.2 Non-Functional Requirements (NFR)

- **NFR-01 (Transactional Integrity & Concurrency):** Slot booking must be executed within an ACID transaction with isolation to guarantee zero double-booking even under concurrent requests.
- **NFR-02 (Portability & Containerization):** The application must package into a single, self-contained executable JAR/WAR deployable to standalone Tomcat, embedded Tomcat, or a minimal Docker container.
- **NFR-03 (Testability):** All user journeys must expose standard HTML identifiers (`id`, `name`, `data-testid`) to support automated end-to-end testing via Selenium WebDriver.
- **NFR-04 (Observability):** Health check and telemetry endpoints must be exposed under `/actuator/health` for monitoring by Jenkins, Docker, and Ansible.
- **NFR-05 (Performance):** Page render times must not exceed 200ms on a standard local environment with in-memory H2 database.

---

## 2. Use Case Model & Diagram

```mermaid
flowchart LR
    Client(("Client / Citizen"))
    Notary(("Certified Notary"))
    DevOps(("DevOps / SysAdmin"))

    subgraph ENotarySystem["Containerized E-Notary Appointment System"]
        UC1["UC-01: Register & Login"]
        UC2["UC-02: Browse Available Slots"]
        UC3["UC-03: Submit Booking Request"]
        UC4["UC-04: View Client Dashboard"]
        UC5["UC-05: Configure Availability Schedule"]
        UC6["UC-06: Confirm / Reject Appointment"]
        UC7["UC-07: Cancel Appointment & Reason"]
        UC8["UC-08: Public Status Tracking"]
        UC9["UC-09: Health & Actuator Telemetry"]
    end

    Client --> UC1
    Client --> UC2
    Client --> UC3
    Client --> UC4
    Client --> UC7
    Client --> UC8

    Notary --> UC1
    Notary --> UC5
    Notary --> UC6
    Notary --> UC7

    DevOps --> UC9
```

---

## 3. Application Architecture Diagram

The system employs a classic, decoupled **3-Tier Layered Architecture**:

```mermaid
flowchart TD
    subgraph ClientTier["Client / Browser Tier"]
        Browser["Web Browser (Desktop / Mobile)"]
        Selenium["Selenium WebDriver Test Suite"]
    end

    subgraph AppTier["Spring Boot Application Tier (Embedded / Standalone Tomcat)"]
        subgraph PresentationLayer["Presentation Layer (Spring MVC)"]
            AuthController["AuthController<br/>(/login, /register)"]
            SlotController["SlotController<br/>(/slots)"]
            BookingController["BookingController<br/>(/bookings, /dashboard)"]
            HealthController["Actuator / Health<br/>(/actuator/health)"]
            ThymeleafViews["Thymeleaf HTML5 Templates<br/>(Bootstrap 5 SSR)"]
        end

        subgraph ServiceLayer["Business Logic Layer"]
            UserService["UserService"]
            SlotService["SlotService"]
            BookingService["BookingService"]
        end

        subgraph DataLayer["Data Access Layer (Spring Data JPA)"]
            UserRepo["UserRepository"]
            NotaryRepo["NotaryRepository"]
            SlotRepo["AppointmentSlotRepository"]
            BookingRepo["BookingRequestRepository"]
        end
    end

    subgraph DataStorage["Data Storage Tier"]
        H2["In-Memory H2 Database<br/>(Local Dev & CI Tests)"]
        Postgres["PostgreSQL Database<br/>(Containerized Prod)"]
    end

    Browser --> PresentationLayer
    Selenium --> PresentationLayer
    PresentationLayer --> ServiceLayer
    ServiceLayer --> DataLayer
    DataLayer --> H2
    DataLayer --> Postgres
```

---

## 4. Domain Data Model & Entity Relationship (ER) Diagram

```mermaid
erDiagram
    USERS ||--o{ NOTARIES : "has profile"
    NOTARIES ||--o{ APPOINTMENT_SLOTS : "publishes"
    USERS ||--o{ BOOKING_REQUESTS : "initiates"
    APPOINTMENT_SLOTS ||--o| BOOKING_REQUESTS : "reserved by"

    USERS {
        bigint id PK
        string full_name
        string email UK
        string password_hash
        string phone
        string role "CLIENT, NOTARY, ADMIN"
        timestamp created_at
    }

    NOTARIES {
        bigint id PK
        bigint user_id FK
        string license_number UK
        string office_address
        string specialization
    }

    APPOINTMENT_SLOTS {
        bigint id PK
        bigint notary_id FK
        date slot_date
        time start_time
        time end_time
        boolean is_booked
    }

    BOOKING_REQUESTS {
        bigint id PK
        string reference_code UK
        bigint client_id FK
        bigint slot_id FK
        string document_type "AFFIDAVIT, DEED, POA"
        string notes
        string status "REQUESTED, CONFIRMED, REJECTED, CANCELLED, COMPLETED"
        string cancellation_reason
        timestamp created_at
        timestamp updated_at
    }
```

---

## 5. API & Web Endpoints Specification

| HTTP Method | Route / Endpoint | Access Level | Description | Payload / Parameters |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/` | Public | Home landing page with navigation and feature highlights | None |
| `GET` | `/login` | Public | Authentication login page | None |
| `GET` | `/register` | Public | User registration page (Client or Notary) | None |
| `POST` | `/register` | Public | Process user registration | `fullName`, `email`, `password`, `role`, `phone` |
| `GET` | `/slots` | Public / Auth | View available slots for selected notary and date | `notaryId`, `date` (YYYY-MM-DD) |
| `POST` | `/slots/generate` | Notary | Generate availability slots for a date range | `notaryId`, `date`, `startTime`, `endTime` |
| `GET` | `/bookings/new` | Client | Booking request form for a specific slot | `slotId` |
| `POST` | `/bookings` | Client | Submit a booking request and generate reference | `slotId`, `documentType`, `notes` |
| `GET` | `/bookings/track` | Public | Public appointment tracking search page | `refCode` (optional) |
| `GET` | `/dashboard/client` | Client | Client's personal booking management dashboard | None (Session) |
| `GET` | `/dashboard/notary` | Notary | Notary's schedule review and approval dashboard | None (Session) |
| `POST` | `/bookings/{id}/confirm`| Notary | Confirm a requested appointment | `id` |
| `POST` | `/bookings/{id}/cancel` | Client/Notary| Cancel an appointment with a mandatory reason | `id`, `cancellationReason` |
| `GET` | `/actuator/health` | Public | Health status check for Jenkins & Ansible | None |

---

## 6. Technology Stack Decision Matrix

| Layer | Selection | Justification |
| :--- | :--- | :--- |
| **Language** | Java 17 LTS | Modern LTS release with high stability, standard in enterprise and academic curricula. |
| **Framework** | Spring Boot 3.2.x | Rapid development, built-in production metrics (`Actuator`), seamless database ORM. |
| **Build Tool** | Apache Maven 3.9+ | Standardized dependency management; mandated for Jenkins build and Selenium test runs. |
| **View Engine** | Thymeleaf + Bootstrap 5 | Zero frontend build step required (no webpack/vite overhead); deterministic DOM elements for Selenium. |
| **Persistence** | Spring Data JPA + Hibernate | Clean repository pattern with automatic query derivation and schema generation. |
| **Testing** | JUnit 5, AssertJ, Selenium WebDriver | Comprehensive unit, integration, and UI testing running natively via `./mvnw test`. |

---

## 7. Working Local Setup Instructions

### Prerequisites
- Java 17+ (`java -version`)
- Maven 3.9+ or bundled Maven Wrapper (`./mvnw`)

### Build & Run Commands
```bash
# 1. Clean and compile the project
./mvnw clean compile

# 2. Run unit and integration tests
./mvnw test

# 3. Package the executable JAR
./mvnw clean package -DskipTests

# 4. Run the application locally
./mvnw spring-boot:run
```

Once started, the application is accessible at:
- **Web Portal:** `http://localhost:8080/`
- **H2 In-Memory Console:** `http://localhost:8080/h2-console`
- **Health Check Endpoint:** `http://localhost:8080/actuator/health`
