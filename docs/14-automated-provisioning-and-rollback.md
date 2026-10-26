# Stage 14 — Automated Provisioning, Idempotency & Rollback Demonstration

## 1. Overview & Pedagogical Objectives
In this final coursework stage, we verified two essential principles of modern Site Reliability Engineering (SRE) and DevOps:
1. **Idempotent Infrastructure Provisioning**: Proving that repeated playbook runs produce zero side effects or changes (`changed=0`) when the system is already in the desired state.
2. **Automated Rollback & Self-Healing**: Demonstrating that in the event of an update failure, a dedicated rollback playbook (`ansible/rollback.yml`) restores the previous known-good version and validates application health.

---

## 2. Idempotent Provisioning Demonstration

### 2.1 First Execution: Initial Deployment (`changed > 0`)
Command:
```bash
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml -e version=1.0.0
```
Execution log:
```
TASK [=== Task 3: Determine Deployment Action (Idempotency Check) ===]
ok: [localhost]

TASK [Stop and remove outdated or unhealthy container]
changed: [localhost]

TASK [Launch container with target version]
changed: [localhost]

TASK [=== Task 6: Validate Application Health Check ===]
FAILED - RETRYING: [localhost]: === Task 6: Validate Application Health Check === (19 retries left).
FAILED - RETRYING: [localhost]: === Task 6: Validate Application Health Check === (18 retries left).
ok: [localhost]

TASK [=== Task 7: Report Deployment Summary ===]
ok: [localhost] => {
    "msg": [
        "Status: DEPLOYED (State Changed)",
        "Active Container: e-notary-production-container",
        "Image: localhost:5000/e-notary-system:1.0.0",
        "Port: http://localhost:8082",
        "Health: UP"
    ]
}

PLAY RECAP *********************************************************************
localhost                  : ok=13   changed=2    unreachable=0    failed=0    skipped=0
```
- **State Result**: Container updated to `1.0.0`, `changed=2` (stopped old instance, started new container).

---

### 2.2 Second Execution: Idempotency Verification (`changed=0`)
Executing the exact same command a second time:
```bash
ansible-playbook -i ansible/inventory.ini ansible/playbook.yml -e version=1.0.0
```
Execution log:
```
TASK [Check if container is currently running]
ok: [localhost]

TASK [Check container health status if running]
ok: [localhost]

TASK [=== Task 3: Determine Deployment Action (Idempotency Check) ===]
ok: [localhost]

TASK [=== Task 4: Pull Target Container Image ===]
skipping: [localhost]

TASK [Stop and remove outdated or unhealthy container]
skipping: [localhost]

TASK [Launch container with target version]
skipping: [localhost]

TASK [=== Task 6: Validate Application Health Check ===]
skipping: [localhost]

TASK [=== Task 7: Report Deployment Summary ===]
ok: [localhost] => {
    "msg": [
        "Status: IDEMPOTENT (No Changes Required)",
        "Active Container: e-notary-production-container",
        "Image: localhost:5000/e-notary-system:1.0.0",
        "Port: http://localhost:8082",
        "Health: UP"
    ]
}

PLAY RECAP *********************************************************************
localhost                  : ok=9    changed=0    unreachable=0    failed=0    skipped=4
```
- **Idempotency Proof**: Notice `changed=0`. Ansible correctly detected that the container was already running `1.0.0` and was healthy. It skipped all disruptive tasks, ensuring zero downtime and zero redundant operations.

---

## 3. Automated Rollback Demonstration

### 3.1 Rollback Architecture (`ansible/rollback.yml`)
When an incident occurs:
1. `ansible/rollback.yml` retrieves the last verified stable image tag from `/tmp/e-notary-active-image.txt` (or an explicit `-e rollback_version=...` parameter).
2. It asserts the presence of a valid target image.
3. It force-removes the unhealthy or degraded container (`docker rm -f`).
4. It launches the previous known-good image with identical production port bindings and environment variables.
5. It validates service restoration via the `/actuator/health` endpoint probe.

### 3.2 Rollback Execution & Evidence
Command:
```bash
ansible-playbook -i ansible/inventory.ini ansible/rollback.yml -e rollback_version=9
```
Execution log:
```
PLAY [Automated Rollback for Containerized E-Notary System] ********************

TASK [Verify a valid rollback target was identified]
ok: [localhost] => {
    "changed": false,
    "msg": "All assertions passed"
}

TASK [Display Rollback Plan]
ok: [localhost] => {
    "msg": [
        "Triggering Automated Rollback...",
        "Restoring Known-Good Target: localhost:5000/e-notary-system:9",
        "Host Port: 8082"
    ]
}

TASK [=== Task 2: Terminate Unhealthy Container ===]
changed: [localhost]

TASK [=== Task 3: Launch Restored Known-Good Container ===]
changed: [localhost]

TASK [=== Task 4: Validate Restored Health Check ===]
FAILED - RETRYING: [localhost]: (19 retries left).
FAILED - RETRYING: [localhost]: (18 retries left).
ok: [localhost]

TASK [=== Task 5: Rollback Complete ===]
ok: [localhost] => {
    "msg": [
        "AUTOMATED ROLLBACK SUCCESSFUL!",
        "Active Container: e-notary-production-container",
        "Restored Image: localhost:5000/e-notary-system:9",
        "Health Status: UP"
    ]
}

PLAY RECAP *********************************************************************
localhost                  : ok=8    changed=2    unreachable=0    failed=0    skipped=1
```

### 3.3 Post-Rollback Service Health Verification
```bash
curl -s http://localhost:8082/actuator/health
```
Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 212582006784,
        "free": 98495213568,
        "threshold": 10485760,
        "path": "/app/.",
        "exists": true
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

Active container verification:
```bash
docker ps --filter "name=e-notary-production-container"
# IMAGE: localhost:5000/e-notary-system:9 | STATUS: Up (healthy) | PORTS: 0.0.0.0:8082->8080/tcp
```

---

## 4. Coursework Deliverables & Milestone Summary

With the completion of Stages 13 and 14, **all 14 stages of the coursework roadmap have been completed, verified with live execution evidence, and fully documented**:

| Stage | Document | Key Deliverable | Status |
|---|---|---|---|
| **00** | `docs/0-init.md` | Agentic baseline, stack decisions, architecture plan | Completed |
| **01** | `docs/01-problem-definition-and-scope.md` | Needs analysis, stakeholder identification, 15-task MVP boundary | Completed |
| **02** | `docs/02-agile-planning-and-devops-workflow.md` | User stories, Kanban backlog, Definition of Done, DevOps lifecycle | Completed |
| **03** | `docs/03-requirements-architecture-and-setup.md` | Architecture diagram, API specification, data models, Maven setup | Completed |
| **04** | `docs/04-git-and-github-initialization.md` | Git branching policies, skeleton commit, issue templates | Completed |
| **05** | `docs/05-feature-development-with-branching.md` | Core workflow on feature branch, PR, code review | Completed |
| **06** | `docs/06-mvp-completion-and-collaboration.md` | 6 MVP features, simulated & resolved merge conflict, v1.0.0-mvp tag | Completed |
| **07** | `docs/07-jenkins-ci-job.md` | Jenkins CI job, Maven build trigger, artifact archiving | Completed |
| **08** | `docs/08-pipeline-as-code-and-deployment.md` | Declarative `Jenkinsfile`, environment parameterization, Tomcat deployment | Completed |
| **09** | `docs/09-selenium-testing-suite.md` | Headless Chromium Selenium E2E suite with automated failure screenshots | Completed |
| **10** | `docs/10-continuous-testing-in-jenkins.md` | JUnit quality gate, intentional defect injection & fix verification | Completed |
| **11** | `docs/11-docker-container-lifecycle.md` | Multi-stage `Dockerfile`, build, run, logs, inspection, cleanup | Completed |
| **12** | `docs/12-jenkins-docker-continuous-deployment.md` | Jenkins Docker CD pipeline, registry publish, container deployment | Completed |
| **13** | `docs/13-ansible-configuration-management.md` | Server prerequisites, Ansible inventory, configuration management | Completed |
| **14** | `docs/14-automated-provisioning-and-rollback.md` | Idempotent provisioning (`changed=0`), health check, automated rollback | Completed |
