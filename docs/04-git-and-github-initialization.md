# Stage 4: Git and GitHub Repository Initialization

| Metadata | Details |
| :--- | :--- |
| **Project Name** | Containerized E-Notary Appointment System |
| **Coursework Reference** | `docs/devops-project-guidelines.ods` (Row 39) |
| **Student** | Aryan Racha (Roll No: `23102C0055`) |
| **Deliverables Covered** | Repository URL, README, Initial Commits, Issues & PR Templates, Branch Policy |

---

## 1. Git Repository Architecture & Branching Policy

To maintain high code quality and adhere to professional DevOps standards, the project adopts a strict Git branching strategy based on GitFlow:

```
[main]          ───●────────────────────────────────────────────● (v1.0.0-mvp)
                    \                                          /
[develop]            ───●───────────────●─────────────────────●
                         \             / \                   /
[feature/...]             ──●─────────●   ──●───────────────●
```

### Branch Rules and Conventions
1. **`main` (Production Branch):**
   - Protected branch containing exclusively stable, deployable releases.
   - Direct commits are prohibited.
   - Merges only accepted from `develop` accompanied by a release tag (e.g., `v1.0.0-mvp`).
2. **`develop` (Integration Branch):**
   - Default active working branch.
   - Aggregates completed features before testing and release staging.
3. **`feature/<feature-name>` (Feature Branches):**
   - Branched from `develop`.
   - Naming convention: `feature/user-auth`, `feature/slot-viewing`, `feature/booking-request`.
   - Merged back into `develop` via Pull Requests with passing CI checks.
4. **`hotfix/<fix-name>` (Urgent Patches):**
   - Branched directly from `main` to address critical defects, merged back to both `main` and `develop`.

---

## 2. Repository Configuration & Directory Structure

```text
devops-project/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.md           # Standardized defect reporting template
│   │   └── feature_request.md      # New functionality request template
│   └── pull_request_template.md    # PR review & DoD checklist
├── .gitignore                      # Ignore build artifacts, IDE configs, logs
├── .mvn/
│   └── wrapper/                    # Self-contained Maven wrapper configuration
├── docs/                           # Coursework milestone deliverables (0-14)
│   ├── 0-init.md
│   ├── 01-problem-definition-and-scope.md
│   ├── 02-agile-planning-and-devops-workflow.md
│   ├── 03-requirements-architecture-and-setup.md
│   ├── 04-git-and-github-initialization.md
│   └── devops-project-guidelines.ods
├── src/
│   ├── main/
│   │   ├── java/com/enotary/
│   │   │   ├── ENotaryApplication.java
│   │   │   └── controller/
│   │   │       └── HomeController.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/
│   │           └── index.html
│   └── test/
│       └── java/com/enotary/
│           └── ENotaryApplicationTests.java
├── AGENTS.md                       # Agentic pair-programming rules & guidelines
├── mise.toml                       # Tool version pinning (Java 17 LTS, Maven 3.9)
├── mvnw & mvnw.cmd                 # Executable Maven Wrapper scripts
├── pom.xml                         # Project Object Model build definition
└── README.md                       # Primary repository documentation
```

---

## 3. GitHub Issue & Pull Request Templates

To guarantee standardized issue tracking during the coursework:
- **`bug_report.md`:** Enforces reproduction steps, environment specifications (OS, Java version, browser), and log attachments.
- **`feature_request.md`:** Requires clear user story framing, acceptance criteria checkboxes, and motivation.
- **`pull_request_template.md`:** Mandates local test verification proof, links to backlog tasks (`TASK-XX`), and the 6-point Definition of Done sign-off.

---

## 4. Git Initialization Commands Executed

```bash
# 1. Initialize local repository
git init -b main

# 2. Stage foundational files
git add .gitignore README.md AGENTS.md mise.toml pom.xml mvnw mvnw.cmd .mvn .github docs src

# 3. Create initial skeleton commit
git commit -m "chore: initialize project skeleton with Spring Boot 3, Maven wrapper, and docs"

# 4. Create and checkout develop branch
git checkout -b develop
```

---

## 5. Verification & Completion Evidence

- [x] Git repository initialized with `main` and `develop` branches.
- [x] Standard `.gitignore` prevents check-in of build artifacts (`target/`), logs, and IDE caches.
- [x] GitHub issue and pull request templates configured under `.github/`.
- [x] Comprehensive `README.md` created with badges, architecture overview, and quick-start instructions.
- [x] Complete project skeleton compiles cleanly with Maven wrapper.
