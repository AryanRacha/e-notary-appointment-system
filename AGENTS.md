# AGENTS.md — Containerized E-Notary Appointment System

## Project Identity & Context
- **Project:** Containerized E-Notary Appointment System
- **Owner:** Aryan Racha (Roll No: `23102C0055`)
- **Primary Source of Truth:** `docs/devops-project-guidelines.ods` (Row 39)
- **Tech Stack:** Java 17, Spring Boot 3, React.js (Vite SPA), Maven, H2/PostgreSQL, Jenkins, Selenium WebDriver, Docker, Ansible.

## Working Modes & Pedagogical Alignment
1. **Interactive Pair-Programming & Learning**:
   - The user has strong backend fundamentals and codes in TypeScript and Python, with Java knowledge up to the Collections Framework.
   - Explain Java/Spring Boot and DevOps patterns by relating them to TS/Python equivalents (e.g., Maven `pom.xml` vs `package.json`/`pyproject.toml`, Spring annotations vs NestJS/FastAPI decorators, Thymeleaf vs Jinja/EJS).
   - Before taking non-trivial architectural or toolchain decisions, explain the options and trade-offs, and ask for user input.
2. **Progressive Step-by-Step Documentation**:
   - As each stage of the coursework is undertaken, document its rationale, design, implementation, and verification evidence under `docs/` using the zero-padded convention:
     - `docs/0-init.md` (Agentic setup, baseline, stack decisions)
     - `docs/01-problem-definition-and-scope.md` through `docs/14-automated-provisioning-and-rollback.md`
   - Keep docs up to date as the single source of truth for coursework deliverables.

## 14-Stage Coursework Roadmap
1. **01-problem-definition-and-scope**: Needs analysis, stakeholder identification, 15-task MVP boundary.
2. **02-agile-planning-and-devops-workflow**: User stories, Kanban backlog, Definition of Done, DevOps lifecycle diagram.
3. **03-requirements-architecture-and-setup**: Architecture diagram, API specification, data models, Maven setup.
4. **04-git-and-github-initialization**: Git branching policies, skeleton commit, issue templates.
5. **05-feature-development-with-branching**: Core workflow implementation on a feature branch, PR, code review.
6. **06-mvp-completion-and-collaboration**: Complete 6 MVP features, simulate & resolve merge conflict, tag release.
7. **07-jenkins-ci-job**: Jenkins setup, Maven build trigger, artifact archiving.
8. **08-pipeline-as-code-and-deployment**: Declarative `Jenkinsfile`, environment parameterization, Tomcat/Nginx deployment.
9. **09-selenium-testing-suite**: Selenium WebDriver E2E test suite running through Maven with failure screenshots.
10. **10-continuous-testing-in-jenkins**: Jenkins test reporting quality gate, intentional defect injection & fix verification.
11. **11-docker-container-lifecycle**: Multi-stage `Dockerfile`, container build, run, inspection, logs, cleanup.
12. **12-jenkins-docker-continuous-deployment**: Pipeline extension to publish versioned image to registry and run container.
13. **13-ansible-configuration-management**: Server prerequisites specification, Ansible inventory and playbook.
14. **14-automated-provisioning-and-rollback**: Idempotent provisioning, health check validation, automated rollback demonstration.

## Quality & Execution Principles
- **Idempotency & Verifiability**: Every command, script, or configuration should be testable and repeatable.
- **Fail-Fast Testing**: Tests must block downstream deployments if assertions fail.
- **Documentation Integrity**: Maintain exact records of commands, configurations, and deliverables required by the course syllabus.
