# Stage 08: Pipeline as Code and Server Deployment

| Metadata | Details |
| :--- | :--- |
| **Student Name** | Aryan Racha |
| **Roll Number** | `23102C0055` |
| **Coursework Project** | Containerized E-Notary Appointment System |
| **Reference Guideline** | `docs/devops-project-guidelines.ods` (Row 39, Stage 8) |
| **Pipeline Definition** | Declarative `Jenkinsfile` in Git SCM |
| **Parameterized Variables** | `ENVIRONMENT` (`development`, `staging`, `production`), `SERVER_PORT` (`8080`), `RUN_INTEGRATION_TESTS` (`true`/`false`) |
| **Deployment Target** | Embedded Tomcat Runtime (port `8080`) |
| **Health Validation** | Spring Boot Actuator (`http://localhost:8080/actuator/health`) |
| **Build Result** | `SUCCESS` (Build #3, duration: 36.7s) |
| **Date** | October 2026 |

---

## 1. Objectives & Coursework Requirement

As defined in Row 39 (Stage 8) of the DevOps project guidelines:
> **"Pipeline as Code and Server Deployment:** Create a Jenkinsfile for the E-Notary Appointment System with checkout, build, package and deploy stages. Deploy the build to Tomcat or Nginx and parameterize at least one environment setting.  
> **Deliverable:** Jenkinsfile, successful pipeline run, deployed application URL/screenshot and configuration evidence."

---

## 2. Pedagogical Concept: Pipeline as Code vs Configuration UI

In legacy DevOps workflows, continuous integration jobs were manually configured through web UIs (such as Jenkins Freestyle jobs configured in XML). Modern software engineering adheres to **Pipeline as Code**:

| Concept | Pipeline as Code (`Jenkinsfile`) | Modern Equivalent in TS / Python |
| :--- | :--- | :--- |
| **Configuration Model** | Declarative Groovy DSL versioned in Git alongside source code | GitHub Actions YAML (`.github/workflows/*.yml`) or GitLab CI (`.gitlab-ci.yml`) |
| **Portability & Auditability** | Every branch can modify its own pipeline; pipeline changes are code-reviewed in PRs | Same: workflow changes follow Git branch/PR reviews |
| **Environment Parameterization** | Interactive prompts or API arguments (`params.ENVIRONMENT`, `params.SERVER_PORT`) | GitHub Actions `workflow_dispatch` inputs / Environment variables |
| **Deployment Gate** | Conditional execution based on parameter flags (`when { expression { ... } }`) | Job / Step conditionals (`if: inputs.run_tests == true`) |
| **Runtime Persistence** | Process decoupling (`BUILD_ID=dontKillMe`, `JENKINS_NODE_COOKIE=dontKillMe`) | Background process supervisors (`supervisord`, `systemd`, `nohup`) |

---

## 3. Declarative Jenkinsfile Specification

The complete [`Jenkinsfile`](file:///home/aryanracha/Coding/college/devops-project/Jenkinsfile) defines 6 sequential stages:

```groovy
pipeline {
    agent any

    parameters {
        choice(
            name: 'ENVIRONMENT',
            choices: ['development', 'staging', 'production'],
            description: 'Target Deployment Environment'
        )
        string(
            name: 'SERVER_PORT',
            defaultValue: '8080',
            description: 'HTTP Port for Application Deployment (Parameterized Environment Setting)'
        )
        booleanParam(
            name: 'RUN_INTEGRATION_TESTS',
            defaultValue: true,
            description: 'Run automated regression and integration tests'
        )
    }

    environment {
        APP_NAME = 'e-notary-appointment-system'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "=== Stage 1: Checkout Source Code from GitHub ==="
                checkout scm
                sh 'git log -1 --oneline'
            }
        }

        stage('Environment Information') {
            steps {
                echo "=== Stage 2: Inspecting Build Environment & Parameterization ==="
                sh '''
                    echo "Target Environment: ${ENVIRONMENT}"
                    echo "Configured Server Port: ${SERVER_PORT}"
                    ./mvnw -version
                '''
            }
        }

        stage('Build & Compile') {
            steps {
                echo "=== Stage 3: Compiling Spring Boot Backend and Building React SPA ==="
                sh './mvnw clean compile'
            }
        }

        stage('Automated Testing') {
            when {
                expression { return params.RUN_INTEGRATION_TESTS }
            }
            steps {
                echo "=== Stage 4: Executing Unit, Integration, and Regression Tests ==="
                sh './mvnw test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package Artifact') {
            steps {
                echo "=== Stage 5: Packaging Standalone Executable Artifact ==="
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    echo "Archiving deployable JAR artifact..."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: false
                }
            }
        }

        stage('Deploy to Server') {
            steps {
                echo "=== Stage 6: Parameterized Server Deployment ==="
                echo "Deploying application to Tomcat embedded runtime on port ${params.SERVER_PORT} in ${params.ENVIRONMENT} mode..."
                sh '''
                    # Stop any existing process running on the target port if necessary
                    PID=$(lsof -ti :${SERVER_PORT} || true)
                    if [ -n "$PID" ]; then
                        echo "Stopping existing process on port ${SERVER_PORT} (PID: $PID)..."
                        kill -9 $PID || true
                    fi

                    # Launch the packaged application in background with parameterized port
                    BUILD_ID=dontKillMe JENKINS_NODE_COOKIE=dontKillMe nohup java -Dserver.port=${SERVER_PORT} -Dspring.profiles.active=${ENVIRONMENT} -jar target/e-notary-appointment-system-*.jar > app-deployment.log 2>&1 &
                    
                    # Health check validation loop
                    echo "Validating deployment health on http://localhost:${SERVER_PORT}/actuator/health..."
                    for i in $(seq 1 30); do
                        if curl -s http://localhost:${SERVER_PORT}/actuator/health | grep '"status":"UP"'; then
                            echo "Application successfully deployed and healthy on port ${SERVER_PORT}!"
                            break
                        fi
                        echo "Waiting for application to boot... ($i/30)"
                        sleep 2
                    done
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully! Artifact packaged and deployed."
        }
        failure {
            echo "Pipeline failed! Inspect console logs and test reports."
        }
    }
}
```

---

## 4. Pipeline Execution & Console Log

The pipeline was executed with the following parameterized settings:
- **`ENVIRONMENT`**: `staging`
- **`SERVER_PORT`**: `8080`
- **`RUN_INTEGRATION_TESTS`**: `true`

### Execution Command:
```bash
java -jar /tmp/jenkins-cli.jar -s http://localhost:8081/ build e-notary-pipeline \
  -p ENVIRONMENT=staging \
  -p SERVER_PORT=8080 \
  -p RUN_INTEGRATION_TESTS=true -f -v
```

### Full Console Output:
```text
Started e-notary-pipeline #3
Started from command line by anonymous
Obtained Jenkinsfile from git /home/aryanracha/Coding/college/devops-project
[Pipeline] Start of Pipeline
[Pipeline] node
Running on Jenkins in /home/aryanracha/.jenkins-server/home/workspace/e-notary-pipeline
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Declarative: Checkout SCM)
[Pipeline] checkout
Selected Git installation does not exist. Using default
The recommended git tool is: NONE
 > git rev-parse --resolve-git-dir /home/aryanracha/.jenkins-server/home/workspace/e-notary-pipeline/.git # timeout=10
Fetching changes from the remote Git repository
 > git config remote.origin.url /home/aryanracha/Coding/college/devops-project # timeout=10
Fetching upstream changes from /home/aryanracha/Coding/college/devops-project
 > git --version # timeout=10
 > git --version # 'git version 2.50.1'
 > git fetch --tags --force --progress -- /home/aryanracha/Coding/college/devops-project +refs/heads/*:refs/remotes/origin/* # timeout=10
 > git rev-parse refs/remotes/origin/develop^{commit} # timeout=10
Checking out Revision 6a248858a7da5844855a81ca0bfb43445adcf0d1 (refs/remotes/origin/develop)
 > git config core.sparsecheckout # timeout=10
 > git checkout -f 6a248858a7da5844855a81ca0bfb43445adcf0d1 # timeout=10
Commit message: "ci: simplify pipeline post actions"
[Pipeline] }
[Pipeline] // stage
[Pipeline] withEnv
[Pipeline] {
[Pipeline] stage
[Pipeline] { (Checkout)
[Pipeline] echo
=== Stage 1: Checkout Source Code from GitHub ===
[Pipeline] checkout
...
[Pipeline] sh
+ git log -1 --oneline
6a24885 ci: simplify pipeline post actions
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Environment Information)
[Pipeline] echo
=== Stage 2: Inspecting Build Environment & Parameterization ===
[Pipeline] sh
+ echo 'Target Environment: staging'
Target Environment: staging
+ echo 'Configured Server Port: 8080'
Configured Server Port: 8080
+ ./mvnw -version
Apache Maven 3.9.9
Java version: 27, vendor: Homebrew
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Build & Compile)
[Pipeline] echo
=== Stage 3: Compiling Spring Boot Backend and Building React SPA ===
[Pipeline] sh
+ ./mvnw clean compile
[INFO] Scanning for projects...
[INFO] Building e-notary-appointment-system 1.0.0-SNAPSHOT
[INFO] --- frontend:1.15.0:npm (npm run build) ---
[INFO] vite v5.4.21 building for production...
[INFO] ✓ built in 280ms
[INFO] Compiling 15 source files with javac [debug target 17] to target/classes
[INFO] BUILD SUCCESS
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Automated Testing)
[Pipeline] echo
=== Stage 4: Executing Unit, Integration, and Regression Tests ===
[Pipeline] sh
+ ./mvnw test
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
Post stage
[Pipeline] junit
Recording test results
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Package Artifact)
[Pipeline] echo
=== Stage 5: Packaging Standalone Executable Artifact ===
[Pipeline] sh
+ ./mvnw package -DskipTests
[INFO] Replacing main artifact with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] BUILD SUCCESS
Post stage
[Pipeline] echo
Archiving deployable JAR artifact...
[Pipeline] archiveArtifacts
Archiving artifacts
Recording fingerprints
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Deploy to Server)
[Pipeline] echo
=== Stage 6: Parameterized Server Deployment ===
[Pipeline] echo
Deploying application to Tomcat embedded runtime on port 8080 in staging mode...
[Pipeline] sh
++ lsof -ti :8080
++ true
+ PID=
+ '[' -n '' ']'
+ echo 'Validating deployment health on http://localhost:8080/actuator/health...'
Validating deployment health on http://localhost:8080/actuator/health...
+ BUILD_ID=dontKillMe
+ JENKINS_NODE_COOKIE=dontKillMe
+ nohup java -Dserver.port=8080 -Dspring.profiles.active=staging -jar target/e-notary-appointment-system-1.0.0-SNAPSHOT.jar
++ seq 1 30
+ for i in $(seq 1 30)
+ curl -s http://localhost:8080/actuator/health
+ grep '"status":"UP"'
+ echo 'Waiting for application to boot... (1/30)'
Waiting for application to boot... (1/30)
+ sleep 2
+ for i in $(seq 1 30)
+ curl -s http://localhost:8080/actuator/health
+ grep '"status":"UP"'
{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}},"diskSpace":{"status":"UP","details":{"total":212582006784,"free":101388095488,"threshold":10485760,"path":"/home/aryanracha/.jenkins-server/home/workspace/e-notary-pipeline/.","exists":true}},"ping":{"status":"UP"}}}
+ echo 'Application successfully deployed and healthy on port 8080!'
Application successfully deployed and healthy on port 8080!
+ break
[Pipeline] }
[Pipeline] // stage
[Pipeline] stage
[Pipeline] { (Declarative: Post Actions)
[Pipeline] echo
Pipeline completed successfully! Artifact packaged and deployed.
[Pipeline] }
[Pipeline] // stage
[Pipeline] }
[Pipeline] // node
[Pipeline] End of Pipeline
Finished: SUCCESS
Completed e-notary-pipeline #3 : SUCCESS
```

---

## 5. Deployed Application Health & Endpoint Verification

### 5.1 Spring Boot Actuator Health Check
```bash
$ curl -s http://localhost:8080/actuator/health | python3 -m json.tool
```
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
                "exists": true,
                "free": 101388095488,
                "path": "/home/aryanracha/.jenkins-server/home/workspace/e-notary-pipeline/.",
                "threshold": 10485760,
                "total": 212582006784
            }
        },
        "ping": {
            "status": "UP"
        }
    }
}
```

### 5.2 Slot Rest API Verification
```bash
$ curl -s http://localhost:8080/api/slots | python3 -m json.tool | head -n 25
```
```json
[
    {
        "id": 1,
        "notary": {
            "id": 1,
            "user": {
                "id": 1,
                "fullName": "Sarah Jenkins, Esq.",
                "email": "sarah.jenkins@notary.org",
                "role": "NOTARY",
                "phone": "+1-555-0101"
            },
            "commissionNumber": "NOT-CA-2024-8841",
            "jurisdiction": "California, USA",
            "status": "ACTIVE"
        },
        "startTime": "2026-10-27T10:00:00",
        "endTime": "2026-10-27T10:30:00",
        "isAvailable": true
    }
]
```

### 5.3 Web SPA UI Verification
```bash
$ curl -s -I http://localhost:8080/
HTTP/1.1 200 
Content-Type: text/html;charset=UTF-8
Content-Length: 1045
Date: Mon, 26 Oct 2026 17:00:55 GMT
```

The React Single-Page Application loads cleanly and mounts the client routing interface.

---

## 6. Deliverables Summary

| Item | Location / Status | Description |
| :--- | :--- | :--- |
| **Pipeline Definition** | [`Jenkinsfile`](file:///home/aryanracha/Coding/college/devops-project/Jenkinsfile) | Declarative 6-stage pipeline as code |
| **Jenkins Job Config** | [`jenkins/pipeline-job-config.xml`](file:///home/aryanracha/Coding/college/devops-project/jenkins/pipeline-job-config.xml) | CpsScmFlowDefinition connecting to Git |
| **Deployment Runtime** | `http://localhost:8080/` | Embedded Tomcat server running parameterized build |
| **Health Check** | `http://localhost:8080/actuator/health` | HTTP 200 `"status": "UP"` with database & disk space |
| **Pipeline Build Evidence** | Build #3: `SUCCESS` | Duration: 36.7s, test reports recorded, JAR archived |
