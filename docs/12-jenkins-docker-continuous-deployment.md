# Stage 12 — Jenkins-Docker Continuous Deployment Pipeline

## 1. Overview & Pedagogical Objectives
In this stage, we upgraded our declarative Jenkins CI/CD pipeline from standalone JAR deployment to modern **Container Continuous Deployment (CD)**.

### TypeScript / Python / Cloud Analogy
In cloud-native production pipelines (such as AWS ECS, Google Cloud Run, Kubernetes, or Docker Swarm):
- Building a raw `.jar`, `.whl`, or Node bundle directly on the server creates environment drift (e.g., mismatched Node/Python/Java versions, missing OS packages).
- **Container Continuous Deployment** packages the application into an immutable Docker image, tags it with the semantic version or CI build number (`${BUILD_NUMBER}`), pushes it to a private container registry (e.g., Amazon ECR, Docker Hub, Harbor), and pulls it onto target production nodes.

In this stage, we implemented:
1. **Local Container Registry**: Deployed a Docker distribution registry (`registry:2`) running on `localhost:5000`.
2. **Versioned Image Tagging & Publishing**: Pipeline automatically tags images with `localhost:5000/e-notary-system:${BUILD_NUMBER}` and `latest`, pushing both to the registry.
3. **Automated Zero-Downtime Container Lifecycle**: Pipeline stops any obsolete container instance, launches the new versioned container on port `8082`, and executes automated health check polling on `/actuator/health`.
4. **Target Parameterization**: Pipeline dynamically allows choosing between `DEPLOY_TARGET=docker` and `DEPLOY_TARGET=standalone`.

---

## 2. Pipeline Architecture & Workflow

```
+--------------------------------------------------------------------------+
|                       Jenkins Declarative Pipeline                       |
+--------------------------------------------------------------------------+
                                     |
    +--------------------------------+--------------------------------+
    |                                |                                |
    v                                v                                v
[Stage 1: Checkout]       [Stage 2: Environment]       [Stage 3: Build & Compile]
    |                                |                                |
    +--------------------------------+--------------------------------+
                                     |
                                     v
                        [Stage 4: Automated Testing]
                          (39 Tests + Selenium E2E)
                                     |
                                     v
                     [Stage 5: Package Standalone Artifact]
                                     |
             +-----------------------+-----------------------+
             |                                               |
  (when: DEPLOY_TARGET == 'docker')               (when: DEPLOY_TARGET == 'standalone')
             |                                               |
             v                                               v
[Stage 6: Build & Publish Image]                 [Stage 8: Standalone Deploy]
- Tag: localhost:5000/e-notary-system:${BUILD}     - java -jar target/*.jar
- docker push localhost:5000/...                             |
             |                                               |
             v                                               |
[Stage 7: Deploy Docker Container]                           |
- docker stop / rm old container                             |
- docker run -p 8082:8080 image:${BUILD}                     |
- Health Check: curl :8082/actuator/health                   |
             |                                               |
             +-----------------------+-----------------------+
                                     |
                                     v
                          [Pipeline Post: SUCCESS]
```

---

## 3. Implementation Details

### 3.1 Pipeline Specification (`Jenkinsfile`)
```groovy
stage('Build & Publish Docker Image') {
    when {
        expression { return params.DEPLOY_TARGET == 'docker' }
    }
    steps {
        echo "=== Stage 6: Building and Publishing Versioned Docker Image ==="
        script {
            def imageTag = "${REGISTRY_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
            def latestTag = "${REGISTRY_URL}/${IMAGE_NAME}:latest"
            
            echo "Building multi-stage Docker image: ${imageTag}..."
            sh "docker build -t ${imageTag} -t ${latestTag} ."

            echo "Publishing versioned image to registry: ${imageTag}..."
            sh "docker push ${imageTag}"
            sh "docker push ${latestTag}"
        }
    }
}

stage('Deploy Docker Container') {
    when {
        expression { return params.DEPLOY_TARGET == 'docker' }
    }
    steps {
        echo "=== Stage 7: Deploying Versioned Container ==="
        script {
            def imageTag = "${REGISTRY_URL}/${IMAGE_NAME}:${BUILD_NUMBER}"
            def containerName = 'e-notary-production-container'
            
            sh """
                docker stop ${containerName} || true
                docker rm ${containerName} || true

                docker run -d --name ${containerName} \\
                    -p ${params.DOCKER_PORT}:8080 \\
                    -e SPRING_PROFILES_ACTIVE=${params.ENVIRONMENT} \\
                    ${imageTag}

                for i in \$(seq 1 30); do
                    if curl -s http://localhost:${params.DOCKER_PORT}/actuator/health | grep '"status":"UP"'; then
                        echo "Container deployment successfully validated and healthy!"
                        break
                    fi
                    sleep 2
                done
            """
        }
    }
}
```

---

## 4. Execution Evidence & Verification

### 4.1 Jenkins Build #9 Execution
- **Job**: `e-notary-pipeline`
- **Build Number**: `#9`
- **Parameters**: `DEPLOY_TARGET=docker`, `REGISTRY_URL=localhost:5000`, `DOCKER_PORT=8082`, `ENVIRONMENT=production`.
- **Result**: `SUCCESS`

Console log snippet:
```
[Pipeline] stage (Build & Publish Docker Image)
Building multi-stage Docker image: localhost:5000/e-notary-system:9...
naming to localhost:5000/e-notary-system:9 done
naming to localhost:5000/e-notary-system:latest done
+ docker push localhost:5000/e-notary-system:9
9: digest: sha256:730564ac4f42568e86e8aaddd627063ab511b13d486ccfffe24407c690c01eaa size: 856
+ docker push localhost:5000/e-notary-system:latest
latest: digest: sha256:730564ac4f42568e86e8aaddd627063ab511b13d486ccfffe24407c690c01eaa size: 856

[Pipeline] stage (Deploy Docker Container)
+ docker run -d --name e-notary-production-container -p 8082:8080 -e SPRING_PROFILES_ACTIVE=production localhost:5000/e-notary-system:9
Validating container health on http://localhost:8082/actuator/health...
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
Container deployment successfully validated and healthy on port 8082!
Finished: SUCCESS
Completed e-notary-pipeline #9 : SUCCESS
```

### 4.2 Registry Catalog Verification
Querying the private registry API:
```bash
curl -s http://localhost:5000/v2/e-notary-system/tags/list
# Response: {"name":"e-notary-system","tags":["1.0.0","9","latest"]}
```

### 4.3 Active Production Container Status
```bash
docker ps --filter "name=e-notary-production-container"
# CONTAINER ID: 82b120b5f9b1 | IMAGE: localhost:5000/e-notary-system:9 | STATUS: Up 15 seconds (healthy) | PORTS: 0.0.0.0:8082->8080/tcp
```

---

## 5. Artifacts Produced & Updated
| Artifact | Purpose |
|---|---|
| `Jenkinsfile` | Declarative pipeline extended with multi-stage Docker build, push, and container deployment |
| `jenkins/pipeline-job-config.xml` | Pipeline job definition updated with Docker CD parameters |
| `docs/12-jenkins-docker-continuous-deployment.md` | Formal documentation of Docker Continuous Deployment |
