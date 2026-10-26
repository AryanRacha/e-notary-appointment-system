# Stage 0: Project Initialization & Agentic Setup

| Metadata | Details |
| :--- | :--- |
| **Student Name** | Aryan Racha |
| **Roll Number** | `23102C0055` |
| **Coursework Project** | Containerized E-Notary Appointment System |
| **Reference Guideline** | `docs/devops-project-guidelines.ods` (Row 39) |
| **Date** | October 2026 |

---

## 1. Project Background & Need

In traditional legal notary services, clients face severe logistical friction:
- Physical travel and long queue times at notary offices.
- Lack of real-time visibility into a notary's schedule and available time slots.
- Disorganized document handling, leading to rescheduled appointments or missing paperwork.
- No automated status tracking or instant cancellation workflows.

The **Containerized E-Notary Appointment System** solves this by providing a streamlined web portal for scheduling, reviewing, confirming, and managing notarial appointments. From an infrastructure perspective, it serves as the end-to-end platform for implementing modern **DevOps practices**: Continuous Integration (Jenkins), Automated Testing (Selenium), Containerization (Docker), Continuous Deployment, and Configuration Management (Ansible).

---

## 2. Technology Stack & Architectural Decisions

Because this project has specific coursework milestones (Maven build execution, Tomcat deployment, Selenium via Maven, Dockerization, and Ansible), the stack has been selected to align with these requirements while remaining maintainable and simple.

### Technology Stack Comparison

| Component | Selected Technology | Alternative Considered | Why Selected | Equivalent in TS / Python |
| :--- | :--- | :--- | :--- | :--- |
| **Backend Framework** | **Java 17 + Spring Boot 3** | Node.js (Express/NestJS) or Python (FastAPI) | Syllabi requirements explicitly mention Maven and Tomcat deployment. Spring Boot provides an embedded Tomcat runtime and can also package as a standard `.war` for standalone Tomcat. | **NestJS** (TS) / **FastAPI** (Python) |
| **Build Tool** | **Apache Maven** (`pom.xml`) | Gradle / Ant | Explicitly required in Stages 3, 7, and 9 (Selenium via Maven). Maven's lifecycle (`clean`, `compile`, `test`, `package`) is standard in Jenkins CI. | `package.json` + `npm` (TS) / `poetry` / `pip` (Python) |
| **Web UI / View** | **Thymeleaf + Bootstrap 5** | React / Next.js / Vue | Server-Side Rendering (SSR) eliminates async hydration delays, making Selenium WebDriver automated testing fast, predictable, and robust. | Jinja2 (Python) / EJS (Node.js) |
| **Database** | **H2 Database (Dev) + PostgreSQL (Prod)** | SQLite / MySQL | H2 runs in-memory with zero local installation for development and CI tests; PostgreSQL is used in Docker/Ansible for production reliability. | SQLite (Dev) / PostgreSQL (Prod) |
| **Continuous Integration** | **Jenkins Pipeline (`Jenkinsfile`)** | GitHub Actions / GitLab CI | Specified in coursework milestones 7, 8, 10, and 12. | GitHub Actions YAML workflows |
| **Automated Testing** | **Selenium WebDriver + JUnit 5** | Cypress / Playwright | Mandated in Milestone 9 to run locally via Maven (`mvn test`). | Playwright / Selenium Python |
| **Containerization** | **Docker** (Multi-stage build) | Podman | Mandated in Milestones 11 & 12. Multi-stage builds keep image size minimal. | Standard Docker containerization |
| **Config Management** | **Ansible** | Puppet | Ansible is agentless (uses SSH), writes declarative YAML playbooks, and makes idempotency testing straightforward compared to Puppet master/agent setups. | Terraform / Ansible |

---

## 3. Documentation Naming Convention

Every milestone will produce its corresponding verified deliverable in `docs/`:

- `docs/0-init.md` — Project Baseline, Tech Stack Evaluation, and Agentic Workflow Setup (Current).
- `docs/01-problem-definition-and-scope.md` — Problem Statement, Stakeholders, Constraints, and 15-Task MVP Scope.
- `docs/02-agile-planning-and-devops-workflow.md` — User Stories, Acceptance Criteria, Scrum Board, Definition of Done, and DevOps Lifecycle Diagram.
- `docs/03-requirements-architecture-and-setup.md` — SRS, Use Case Diagrams, Data Models, API Endpoints, and Skeleton Setup.
- `docs/04-git-and-github-initialization.md` — Git Repo Structure, Branching Policies, Issue Templates, and Initial Skeleton.
- `docs/05-feature-development-with-branching.md` — Feature 1 Development, Git Workflow, PR, Code Review, and Merge.
- `docs/06-mvp-completion-and-collaboration.md` — Full 6-Feature MVP, Git Conflict Creation & Resolution, and Release Tagging (`v1.0.0-mvp`).
- `docs/07-jenkins-ci-job.md` — Jenkins Installation, GitHub Polling/Webhook Trigger, Maven Build, and Artifact Archival.
- `docs/08-pipeline-as-code-and-deployment.md` — Declarative `Jenkinsfile` with Build/Package/Deploy Stages to Tomcat/Nginx.
- `docs/09-selenium-testing-suite.md` — 3-5 E2E User Journeys, Selenium WebDriver Scripts, and Failure Screenshot Mechanisms.
- `docs/10-continuous-testing-in-jenkins.md` — Jenkins Automated Test Quality Gate, Defect Injection, Pipeline Abort, and Resolution.
- `docs/11-docker-container-lifecycle.md` — Multi-stage `Dockerfile`, Container Lifecycle Management, and CLI Command Logs.
- `docs/12-jenkins-docker-continuous-deployment.md` — Automated Docker Build, Registry Push, and Containerized CD Pipeline.
- `docs/13-ansible-configuration-management.md` — Server Prerequisites Specification, Ansible Inventory, and Playbooks.
- `docs/14-automated-provisioning-and-rollback.md` — Clean Node Provisioning, Idempotency Verification, Health Checks, and Rollback Demo.

---

## 4. Collaborative Learning Structure

To ensure a hands-on learning experience:
1. **Conceptual Parallels:** Every Java and DevOps pattern will be linked back to Python and TypeScript equivalents.
2. **Decision Touchpoints:** Key choices (e.g., database schema, API design, test scenarios, pipeline stages) will be presented with trade-offs before implementation.
3. **Reproducibility:** All build, test, container, and configuration steps will be fully verifiable via standard CLI commands.
