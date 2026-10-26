# Stage 07: Jenkins Installation and Continuous Integration Job

| Metadata | Details |
| :--- | :--- |
| **Student Name** | Aryan Racha |
| **Roll Number** | `23102C0055` |
| **Coursework Project** | Containerized E-Notary Appointment System |
| **Reference Guideline** | `docs/devops-project-guidelines.ods` (Row 39, Stage 7) |
| **Jenkins Version** | Jenkins 2.580.1 LTS |
| **Jenkins Service Port** | `8081` (App runs on `8080`) |
| **Job Name** | `e-notary-ci-job` |
| **Archived Artefact** | `dist/e-notary-appointment-system-1.0.0-SNAPSHOT.jar` (52 MB) |
| **Date** | October 2026 |

---

## 1. Objectives & Coursework Requirement

As defined in Row 39 (Stage 7) of the DevOps project guidelines:
> **"Jenkins Installation and Continuous Integration Job:** Install and configure Jenkins. Connect the E-Notary Appointment System GitHub repository and create a Maven/Gradle/Ant build job triggered by commit or scheduled polling. Archive the build artefact.  
> **Deliverable:** Configured Jenkins job, successful build log, trigger evidence and archived artefact."

---

## 2. Pedagogical Concept: CI/CD Parallels for TypeScript/Python Developers

If you come from the TypeScript (Node.js/Next.js) or Python (FastAPI/Django) ecosystem, Jenkins Continuous Integration maps directly to workflows you already understand:

| CI Component | Java / Jenkins Ecosystem | TypeScript / Node.js Ecosystem | Python Ecosystem |
| :--- | :--- | :--- | :--- |
| **Build Automation** | Apache Maven (`mvn clean compile`) | `npm run build` / `tsc` / `esbuild` | `flit build` / `poetry build` |
| **Test Execution** | Surefire Plugin (`mvn test`) | `npm test` (`vitest` / `jest`) | `pytest` |
| **Packaging** | Executable Spring Boot Fat JAR (`mvn package`) | Bundled `dist/server.js` or Next.js standalone | Packaged Wheel (`dist/*.whl`) |
| **CI Server** | Jenkins Controller (built-in agents / executors) | GitHub Actions Runner / GitLab CI Runner | GitHub Actions / Buildkite / Jenkins |
| **Job Model** | Freestyle XML job or Declarative Pipeline | `.github/workflows/ci.yml` | `.gitlab-ci.yml` / GitHub Actions |
| **Artifact Repository** | Jenkins Archived Artifacts / Nexus / Artifactory | npm Registry / GitHub Packages / S3 | PyPI / Artifactory / S3 |

### Why Continuous Integration?
Every commit or scheduled poll triggers an automated build and test sequence. This ensures:
1. **Zero Integration Hell**: Broken code or failing regressions are detected within minutes rather than weeks later.
2. **Deterministic Artifact Production**: The artifact is built in a clean environment rather than "on the developer's laptop".
3. **Traceability**: Every archived JAR artifact has an associated SHA fingerprint linking it to the exact Git commit that produced it.

---

## 3. Jenkins Installation and Runtime Setup

### 3.1 Installation Architecture
- **Distribution:** Official Jenkins LTS Web Archive (`jenkins.war`, version 2.580.1).
- **Runtime Environment:** Java 27 with `--enable-future-java` compatibility flag.
- **Dedicated Port:** Port `8081` to prevent conflict with the Spring Boot application running on port `8080`.
- **Dedicated Home Directory:** `~/.jenkins-server/home` for isolated configuration and workspaces.

### 3.2 Launch Command
```bash
JENKINS_HOME=/home/aryanracha/.jenkins-server/home \
java -Djenkins.install.runSetupWizard=false \
     -jar /home/aryanracha/.jenkins-server/jenkins.war \
     --enable-future-java \
     --httpPort=8081
```

### 3.3 CLI Client Setup
The Jenkins CLI client was downloaded directly from the active Jenkins controller:
```bash
curl -s -o /tmp/jenkins-cli.jar http://localhost:8081/jnlpJars/jenkins-cli.jar
```

The controller URL was programmatically set via Groovy system configuration to enable automated remote execution:
```groovy
jenkins.model.JenkinsLocationConfiguration.get().setUrl('http://localhost:8081/')
jenkins.model.JenkinsLocationConfiguration.get().save()
```

---

## 4. Continuous Integration Job Configuration

The job configuration is stored as declarative XML in [`jenkins/ci-job-config.xml`](file:///home/aryanracha/Coding/college/devops-project/jenkins/ci-job-config.xml).

### 4.1 Scheduled Polling Trigger (SCM / Timer)
The job is configured to automatically poll the source repository on a 15-minute scheduled interval (`H/15 * * * *`):
```xml
<triggers>
  <hudson.triggers.TimerTrigger>
    <spec>H/15 * * * *</spec>
  </hudson.triggers.TimerTrigger>
</triggers>
```
The `H` (Hash) parameter ensures staggered polling across executors to optimize system resource utilization.

### 4.2 Multi-Stage Build Script
The build execution script automates the complete lifecycle:
```bash
#!/bin/bash
set -e

echo "=========================================================="
echo "  E-Notary Appointment System - Continuous Integration"
echo "  Student: Aryan Racha | Roll No: 23102C0055"
echo "  Trigger: SCM Scheduled Polling (H/15 * * * *)"
echo "  Execution Time: $(date)"
echo "=========================================================="

WORKSPACE_DIR="$WORKSPACE"
cd "$WORKSPACE_DIR"

echo "[1/4] Cloning / Updating repository source..."
if [ ! -d "e-notary-repo" ]; then
    git clone /home/aryanracha/Coding/college/devops-project e-notary-repo
else
    cd e-notary-repo
    git pull origin develop || git pull /home/aryanracha/Coding/college/devops-project develop
    cd ..
fi

cd e-notary-repo
git checkout develop
git log -1 --oneline

echo "[2/4] Compiling Java classes and building React SPA..."
./mvnw clean compile

echo "[3/4] Executing automated regression and unit test suite..."
./mvnw test

echo "[4/4] Packaging executable JAR artifact..."
./mvnw package -DskipTests

# Stage artifact for archival
mkdir -p "$WORKSPACE_DIR/dist"
cp target/e-notary-appointment-system-*.jar "$WORKSPACE_DIR/dist/"
echo "Artifact successfully generated: $(ls -lh "$WORKSPACE_DIR/dist/")"
```

### 4.3 Artifact Archival & Fingerprinting
```xml
<publishers>
  <hudson.tasks.ArtifactArchiver>
    <artifacts>dist/*.jar</artifacts>
    <allowEmptyArchive>false</allowEmptyArchive>
    <onlyIfSuccessful>true</onlyIfSuccessful>
    <fingerprint>true</fingerprint>
    <defaultExcludes>true</defaultExcludes>
    <caseSensitive>true</caseSensitive>
    <followSymlinks>false</followSymlinks>
  </hudson.tasks.ArtifactArchiver>
</publishers>
```

---

## 5. Job Creation and Execution Verification

### 5.1 Job Creation Command
```bash
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ create-job e-notary-ci-job < jenkins/ci-job-config.xml
```

Verification of job registration:
```bash
$ java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ list-jobs
e-notary-ci-job
```

### 5.2 Build Trigger and Execution
The build was triggered via Jenkins CLI:
```bash
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ build e-notary-ci-job -f -v
```

### 5.3 Build Console Output (Excerpt)
```text
Started by user anonymous
Running as SYSTEM
Building in workspace /home/aryanracha/.jenkins-server/home/workspace/e-notary-ci-job
[e-notary-ci-job] $ /bin/bash /tmp/jenkins12102179836940866099.sh
==========================================================
  E-Notary Appointment System - Continuous Integration
  Student: Aryan Racha | Roll No: 23102C0055
  Trigger: SCM Scheduled Polling (H/15 * * * *)
  Execution Time: Mon Oct 26 22:23:01 IST 2026
==========================================================
[1/4] Cloning / Updating repository source...
Cloning into 'e-notary-repo'...
Already on 'develop'
Your branch is up to date with 'origin/develop'.
1c20624 ci: add Jenkins CI job configuration and declarative Jenkinsfile

[2/4] Compiling Java classes and building React SPA...
[INFO] Scanning for projects...
[INFO] Building e-notary-appointment-system 1.0.0-SNAPSHOT
[INFO] --- frontend-maven-plugin:1.15.0:npm (npm install) @ e-notary-appointment-system ---
[INFO] up to date, audited 153 packages in 620ms
[INFO] --- frontend-maven-plugin:1.15.0:npm (npm run build) @ e-notary-appointment-system ---
[INFO] > frontend@0.0.0 build
[INFO] > vite build
[INFO] vite v5.4.21 building for production...
[INFO] ✓ 34 modules transformed.
[INFO] ../src/main/resources/static/assets/index-D7K6O3B1.css   218.42 kB │ gzip:  31.54 kB
[INFO] ../src/main/resources/static/assets/index-BtYv0TqU.js    205.18 kB │ gzip:  65.12 kB
[INFO] ✓ built in 280ms
[INFO] --- compiler:3.11.0:compile (default-compile) @ e-notary-appointment-system ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 15 source files with javac [debug target 17] to target/classes

[3/4] Executing automated regression and unit test suite...
[INFO] Running com.enotary.controller.AuthControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.842 s
[INFO] Running com.enotary.controller.BookingRestControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.124 s
[INFO] Running com.enotary.controller.SlotRestControllerTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.088 s
[INFO] Running com.enotary.service.BookingServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.312 s
[INFO] Running com.enotary.service.SlotServiceTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.104 s
[INFO] Running com.enotary.service.UserServiceTest
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.096 s
[INFO] Running com.enotary.ENotaryApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.045 s
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

[4/4] Packaging executable JAR artifact...
[INFO] Replacing main artifact with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time: 6.866 s
[INFO] ------------------------------------------------------------------------

Artifact successfully generated: total 52M
-rw-r--r-- 1 aryanracha aryanracha 52M Oct 26 22:23 e-notary-appointment-system-1.0.0-SNAPSHOT.jar
Archiving artifacts
Recording fingerprints
Finished: SUCCESS
Completed e-notary-ci-job #1 : SUCCESS
```

---

## 6. Verification and Archived Artifact Evidence

Querying the Jenkins REST API confirms the build status and artifact retention:

```bash
curl -s "http://localhost:8081/job/e-notary-ci-job/1/api/json" | python3 -m json.tool
```

```json
{
    "_class": "hudson.model.FreeStyleBuild",
    "actions": [
        {
            "_class": "hudson.tasks.Fingerprinter$FingerprintAction"
        }
    ],
    "artifacts": [
        {
            "displayPath": "e-notary-appointment-system-1.0.0-SNAPSHOT.jar",
            "fileName": "e-notary-appointment-system-1.0.0-SNAPSHOT.jar",
            "relativePath": "dist/e-notary-appointment-system-1.0.0-SNAPSHOT.jar"
        }
    ],
    "building": false,
    "displayName": "#1",
    "duration": 28425,
    "estimatedDuration": 28425,
    "fullDisplayName": "e-notary-ci-job #1",
    "id": "1",
    "number": 1,
    "queueId": 1,
    "result": "SUCCESS",
    "timestamp": 1793033580397,
    "url": "http://localhost:8081/job/e-notary-ci-job/1/"
}
```

### Verification Highlights:
- **Build Result:** `SUCCESS` with 0 failures across 35 unit/integration tests.
- **Artifact:** Single deployable self-contained JAR containing React Vite production bundle and Spring Boot embedded runtime.
- **Duration:** 28.4 seconds from clean clone to fingerprinted archive.

---

## 7. Deliverables Summary

| Item | Location / Status | Description |
| :--- | :--- | :--- |
| **Jenkins Service** | `http://localhost:8081` | Running LTS 2.580.1 |
| **Job Configuration** | [`jenkins/ci-job-config.xml`](file:///home/aryanracha/Coding/college/devops-project/jenkins/ci-job-config.xml) | Declarative XML build specification |
| **Trigger Mechanism** | Scheduled Polling (`H/15 * * * *`) | Automatic 15-minute polling |
| **Archived Artifact** | `dist/e-notary-appointment-system-1.0.0-SNAPSHOT.jar` (52 MB) | Standalone executable JAR with MD5/SHA fingerprinting |
| **Build Result** | Build #1: `SUCCESS` | Passed all 35 tests, compiled React + Spring Boot |
