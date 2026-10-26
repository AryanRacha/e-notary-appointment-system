# Stage 5: Feature Development with Branching

| Metadata | Details |
| :--- | :--- |
| **Project Name** | Containerized E-Notary Appointment System |
| **Coursework Reference** | `docs/devops-project-guidelines.ods` (Row 39) |
| **Student** | Aryan Racha (Roll No: `23102C0055`) |
| **Feature Developed** | Feature 1: User Registration & Authentication (Tasks 01–04) |
| **Feature Branch** | `feature/user-auth` |
| **Target Integration Branch** | `develop` |
| **Deliverables Covered** | Working Feature 1, Feature Branch, Pull Request, Review Comments, Merge Evidence |

---

## 1. Feature 1 Overview & Architectural Implementation

Feature 1 establishes the core identity and security layer of the E-Notary system:
- **Domain Modeling:** Created `User`, `Role` (`CLIENT`, `NOTARY`, `ADMIN`), and `Notary` JPA entities.
- **Cryptographic Security:** Built `PasswordUtil` with salted SHA-256 password hashing and constant-time comparison to prevent timing attacks.
- **Data Access:** Implemented `UserRepository` and `NotaryRepository` leveraging Spring Data derived queries (`findByEmail`, `existsByEmail`, `findByLicenseNumber`).
- **Service Layer:** `UserService` enforces transactional safety, unique email constraints, and role-based notary profile creation.
- **Web & API Presentation:** Implemented `AuthRestController` exposing JSON endpoints (`/api/auth/register`, `/api/auth/login`, `/api/auth/current`, `/api/auth/logout`) and `AuthController` with React SPA views featuring dynamic client-vs-notary registration and session management.

---

## 2. Git Branching & Workflow Lifecycle

Following the project's GitFlow policy, work was performed in an isolated feature branch branched from `develop`:

```bash
# 1. Branch creation
git checkout develop
git checkout -b feature/user-auth

# 2. Incremental, atomic commits
git add src/main/java/com/enotary/model/ src/main/java/com/enotary/repository/ src/main/java/com/enotary/util/
git commit -m "feat: add user, notary, and role domain models with JPA repositories"

git add src/main/java/com/enotary/service/ src/main/java/com/enotary/dto/
git commit -m "feat: implement user registration and authentication service"

git add src/main/java/com/enotary/controller/ src/main/resources/templates/
git commit -m "feat: implement auth web controller and Thymeleaf register/login templates"

git add src/test/
git commit -m "test: add unit and integration test suite for user authentication"

# 3. Pull Request Simulation & Merge into develop
git checkout develop
git merge --no-ff feature/user-auth -m "Merge pull request #1 from feature/user-auth: User Registration & Authentication"
```

---

## 3. Pull Request & Code Review Record

### PR Metadata
* **PR Number:** `#1`
* **Title:** `[FEAT] Implement User Registration and Authentication Module`
* **Source Branch:** `feature/user-auth`
* **Target Branch:** `develop`
* **Associated Backlog Tasks:** `TASK-01`, `TASK-02`, `TASK-03`, `TASK-04`

### Code Review Log

| Reviewer | Component | Comment | Resolution |
| :--- | :--- | :--- | :--- |
| **Lead DevOps Reviewer** | `PasswordUtil.java` | Confirmed that salt generation utilizes `SecureRandom` and verification employs constant-time `MessageDigest.isEqual`. | Approved. |
| **Backend Architecture** | `UserService.java` | Verified that duplicate email checks and duplicate license checks throw `IllegalArgumentException` caught gracefully by `AuthController`. | Approved. |
| **QA / Testing** | `AuthControllerTest.java` | Verified MockMvc coverage for positive login, invalid credentials, duplicate registration, and session invalidation on logout. | Approved. |

**Review Decision:** LGTM (Looks Good To Me) &mdash; Approved and Merged via `--no-ff`.

---

## 4. Test Verification Evidence

Before merging, all unit and integration tests were executed via `./mvnw test`:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.enotary.controller.AuthControllerTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 7.601 s
[INFO] Running com.enotary.ENotaryApplicationTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.135 s
[INFO] Running com.enotary.service.UserServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.019 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

---

## 5. Git Merge Graph Evidence

```text
*   5f73370 (HEAD -> develop) Merge pull request #1 from feature/user-auth: User Registration & Authentication
|\  
| * 457b972 (feature/user-auth) test: add unit and integration test suite for user authentication
| * 0a07fc8 feat: implement auth web controller and Thymeleaf register/login templates
| * b3f5ebb feat: implement user registration and authentication service
| * bd5d446 feat: add user, notary, and role domain models with JPA repositories
|/  
* 88900dd (main) docs: record remote GitHub repository URL
* cb74e5f chore: initialize project skeleton with Spring Boot 3, Maven wrapper, and docs
```

---

## 6. Verification & Completion Checklist

- [x] Feature 1 fully implemented on dedicated branch `feature/user-auth`.
- [x] Standard Git lifecycle demonstrated (`add`, `commit`, branch creation, merge).
- [x] Simulated Pull Request reviewed with formal architectural and security comments.
- [x] Clean `--no-ff` merge commit preserving history created on `develop`.
- [x] 17 automated tests passing with zero failures.
- [x] Deliverable documented under `docs/05-feature-development-with-branching.md`.
