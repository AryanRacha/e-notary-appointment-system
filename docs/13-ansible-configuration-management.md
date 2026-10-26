# Stage 13 — Ansible Configuration Management & Server Provisioning

## 1. Overview & Pedagogical Objectives
In this stage, we introduced **Infrastructure as Code (IaC)** and **Configuration Management** using **Ansible**.

### TypeScript / Python Analogy
In application development, code is version-controlled and deployed deterministically. Configuration Management applies the exact same software engineering rigor to server environments:
- **Imperative Shell Scripts (Anti-pattern)**: Writing bash scripts filled with `apt-get install`, `docker run`, and `sed` commands often fails on subsequent runs, breaks on partial execution, and has no rollback safety.
- **Declarative Configuration Management (Ansible)**: You specify the desired target state of the system in YAML. Ansible inspects the current state of the machine and executes *only* the actions required to achieve that state.
- **Idempotency**: An operation is idempotent if executing it once has the desired effect, and executing it multiple times leaves the system in that exact same state without unintended side effects (`changed=0`).

For the E-Notary system:
We codified our production runtime deployment into declarative Ansible plays, defining host inventories, prerequisites checks, container lifecycle, and Actuator health verification.

---

## 2. Server Prerequisites Specification

To deploy and run the Containerized E-Notary Appointment System, target production nodes must meet the following baseline requirements:

| Prerequisite | Specification | Rationale |
|---|---|---|
| **Host Operating System** | Linux (Ubuntu 22.04+, Debian 12+, Arch, RHEL 9+) | Standard container host OS |
| **Container Engine** | Docker Engine 24.0+ or Podman | Required for executing the production runtime container |
| **Container Registry** | Accessible Docker Registry (e.g., `localhost:5000` or Docker Hub) | Provides versioned, immutable image artifacts |
| **Open Networking Ports** | `8080/tcp` (Standard) or `8082/tcp` (Containerized) | HTTP access to Spring Boot REST endpoints & React SPA |
| **Security Hardening** | Dedicated non-root system user (`appuser:appgroup`, UID 1000) | Enforces least-privilege container security |
| **Diagnostic Utilities** | `curl` or `wget` | Required by container health probes |
| **Python Environment** | Python 3.9+ with `ansible-core` 2.15+ | Required by Ansible controller for local or SSH execution |

---

## 3. Ansible Architecture & Project Structure

The Ansible automation suite is organized under `ansible/`:
```
ansible/
├── ansible.cfg        # Controller configuration (inventory path, YAML callback)
├── inventory.ini      # Environment host mappings (staging, production, localhost)
├── playbook.yml       # Idempotent provisioning & continuous deployment playbook
└── rollback.yml       # Automated failure recovery & rollback playbook
```

### 3.1 Inventory Configuration (`ansible/inventory.ini`)
```ini
[app_servers]
localhost ansible_connection=local

[staging]
localhost ansible_connection=local app_environment=staging host_port=8082

[production]
localhost ansible_connection=local app_environment=production host_port=8082
```

### 3.2 Ansible Settings (`ansible/ansible.cfg`)
```ini
[defaults]
inventory = inventory.ini
host_key_checking = False
retry_files_enabled = False
stdout_callback = yaml
interpreter_python = auto_silent
nocows = 1
```

---

## 4. Playbook Architecture (`ansible/playbook.yml`)

The playbook implements a structured 7-stage provisioning flow:
1. **Prerequisites Validation**: Asserts Docker CLI and daemon responsiveness.
2. **State Inspection & Version Backup**: Queries `docker inspect` for the active image and writes it to `/tmp/e-notary-active-image.txt` to enable deterministic rollback.
3. **Idempotency Gate**: Computes `needs_deployment` by checking if the active image and health match the desired target version.
4. **Image Pulling**: Pulls the versioned image tag from the registry (`docker pull`).
5. **Container Convergence**: Stops outdated instances and launches the new container with restart policies and port bindings.
6. **Health Check Gate**: Uses `ansible.builtin.uri` to poll `http://localhost:{{ host_port }}/actuator/health` until HTTP 200 with `"status":"UP"` is returned.
7. **Execution Summary**: Outputs human-readable deployment status (`DEPLOYED` vs `IDEMPOTENT`).

---

## 5. Artifacts Produced
| File | Description |
|---|---|
| `ansible/ansible.cfg` | Ansible controller configuration |
| `ansible/inventory.ini` | Host inventory definitions for staging and production |
| `ansible/playbook.yml` | Declarative provisioning and container deployment playbook |
| `docs/13-ansible-configuration-management.md` | Formal documentation of server prerequisites and Ansible design |
