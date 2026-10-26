# Stage 11 — Docker Container Lifecycle & Multi-Stage Image Hardening

## 1. Overview & Pedagogical Objectives
In this stage, we containerized the E-Notary Appointment System using a production-grade **multi-stage `Dockerfile`** and executed the complete container lifecycle.

### TypeScript / Python Analogy
In Node.js or Python containerization:
- **Single-stage `Dockerfile` (Anti-pattern)**: Leaves compilers, `npm devDependencies`, `git`, and build tools in the final image, bloating it to over 1GB and leaving a large security attack surface.
- **Multi-Stage `Dockerfile` (Best Practice)**:
  - **Stage 1 (Builder)**: Installs build tools (`gcc`, `npm`, `maven`), runs compilation and tests, generates production bundles/binaries.
  - **Stage 2 (Runtime)**: Copies *only* the compiled assets (`dist/`, `app.jar`, or Python virtual environment) into a minimal base (e.g., Alpine Linux or Distroless), running as an unprivileged non-root user.

For our Java Spring Boot + React Vite application:
- **Build Stage**: Eclipse Temurin JDK 17 Jammy (~400MB base) + Maven dependencies + Node.js (via `frontend-maven-plugin`).
- **Runtime Stage**: Eclipse Temurin JRE 17 Alpine (~165MB total final image size).
- **Hardening**: Created dedicated system user `appuser:appgroup` and integrated native Docker health checks (`HEALTHCHECK`).

---

## 2. Architecture & Multi-Stage Dockerfile

### 2.1 Multi-Stage Build Flow
```
+-------------------------------------------------------------------------+
| STAGE 1: BUILDER (eclipse-temurin:17-jdk-jammy)                         |
|  1. Copy Maven Wrapper (.mvn, mvnw, pom.xml) -> Cache dependency layer  |
|  2. Copy frontend/ & src/                                               |
|  3. Run: ./mvnw clean package -DskipTests -B                            |
|     -> frontend-maven-plugin builds React Vite SPA into static/         |
|     -> spring-boot-maven-plugin packages fat JAR                        |
+-------------------------------------------------------------------------+
                                    |
                    Copies ONLY app.jar (~52MB)
                                    v
+-------------------------------------------------------------------------+
| STAGE 2: RUNTIME (eclipse-temurin:17-jre-alpine)                        |
|  1. addgroup -S appgroup && adduser -S appuser -G appgroup              |
|  2. COPY --from=builder /build/target/*.jar /app/app.jar                |
|  3. USER appuser (Drop root privileges)                                 |
|  4. EXPOSE 8080                                                         |
|  5. HEALTHCHECK CMD wget --spider http://localhost:8080/actuator/health |
|  6. ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]        |
+-------------------------------------------------------------------------+
```

### 2.2 Dockerfile Specification
```dockerfile
# Stage 1: Builder
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B || true

COPY frontend/ frontend/
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Hardened Minimal Production Runtime
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL maintainer="Aryan Racha (Roll No: 23102C0055)"
LABEL description="Containerized E-Notary Appointment System Production Container"
LABEL version="1.0.0"

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/e-notary-appointment-system-*.jar /app/app.jar
RUN chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=production \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
```

---

## 3. Container Lifecycle Demonstration & Verification

### 3.1 Step 1: Build the Multi-Stage Image
Command:
```bash
docker build -t e-notary-system:1.0.0 .
```
Result:
```
=> exporting to image
=> naming to docker.io/library/e-notary-system:1.0.0
```
Image size inspection:
```bash
docker images e-notary-system:1.0.0
# IMAGE: e-notary-system:1.0.0 | CONTENT SIZE: 165MB
```
The multi-stage build condensed the entire JDK, Node.js runtime, build tools, and dependencies into a production-ready **165MB** footprint.

### 3.2 Step 2: Run the Container Detached
Command:
```bash
docker run -d --name e-notary-app -p 8082:8080 -e SPRING_PROFILES_ACTIVE=production e-notary-system:1.0.0
```
Container process status (`docker ps`):
```
CONTAINER ID   IMAGE                   COMMAND                  STATUS                    PORTS                    NAMES
016c4470a000   e-notary-system:1.0.0   "sh -c 'java $JAVA_O…"   Up 11 seconds (healthy)   0.0.0.0:8082->8080/tcp   e-notary-app
```
*Note: The native container status automatically changed to `(healthy)` following successful execution of the `/actuator/health` probe.*

### 3.3 Step 3: Container Log Inspection (`docker logs`)
Command:
```bash
docker logs --tail 30 e-notary-app
```
Key log evidence:
```
2026-10-26T19:32:51.495Z  INFO 1 --- [main] com.enotary.ENotaryApplication : Starting ENotaryApplication v1.0.0-SNAPSHOT using Java 17.0.20.1 with PID 1 (/app/app.jar started by appuser in /app)
2026-10-26T19:32:51.498Z  INFO 1 --- [main] com.enotary.ENotaryApplication : The following 1 profile is active: "production"
2026-10-26T19:32:56.345Z  INFO 1 --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port 8080 (http) with context path ''
2026-10-26T19:32:56.360Z  INFO 1 --- [main] com.enotary.ENotaryApplication : Started ENotaryApplication in 5.26 seconds
```
Confirms non-root execution (`started by appuser in /app`), production profile activation, and HTTP initialization.

### 3.4 Step 4: Endpoint & Service Validation
Probed live endpoints mapped to host port `8082`:
1. **Actuator Health Probe**:
   ```bash
   curl -s http://localhost:8082/actuator/health
   # Response: {"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
   ```
2. **Notary REST API**:
   ```bash
   curl -s http://localhost:8082/api/notaries
   # Response: HTTP 200 OK with seeded notaries list
   ```
3. **React SPA Static Frontend**:
   ```bash
   curl -s -I http://localhost:8082/
   # Response: HTTP/1.1 200 OK | Content-Type: text/html;charset=UTF-8
   ```

### 3.5 Step 5: Container Metadata Inspection (`docker inspect`)
Command:
```bash
docker inspect --format 'State: {{.State.Status}} | Health: {{.State.Health.Status}} | User: {{.Config.User}}' e-notary-app
```
Output:
```
State: running | Health: healthy | User: appuser
```

### 3.6 Step 6: Lifecycle Cleanup Demonstration
Command:
```bash
docker stop e-notary-app
docker rm e-notary-app
docker ps -a --filter "name=e-notary-app"
```
Confirmed container gracefully stopped, exited, and was removed from the host daemon.

---

## 4. Artifacts Produced
| File | Purpose |
|---|---|
| `Dockerfile` | Multi-stage build definition (Temurin-17 JDK builder + Alpine-17 JRE runtime) |
| `.dockerignore` | Build context optimization excluding target/, logs, and node_modules |
| `docs/11-docker-container-lifecycle.md` | Formal documentation of container build, execution, logs, and cleanup |
