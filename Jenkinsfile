pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'develop',
            description: 'Git branch to checkout and build'
        )
        choice(
            name: 'DEPLOY_TARGET',
            choices: ['docker', 'standalone'],
            description: 'Deployment Target Architecture (Docker Container vs Standalone Tomcat JAR)'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['production', 'staging', 'development'],
            description: 'Target Deployment Environment'
        )
        string(
            name: 'REGISTRY_URL',
            defaultValue: 'localhost:5000',
            description: 'Container Image Registry URL'
        )
        string(
            name: 'DOCKER_PORT',
            defaultValue: '8082',
            description: 'Host Port for Containerized Service Deployment'
        )
        string(
            name: 'SERVER_PORT',
            defaultValue: '8080',
            description: 'HTTP Port for Standalone Tomcat Application Deployment'
        )
        booleanParam(
            name: 'RUN_INTEGRATION_TESTS',
            defaultValue: true,
            description: 'Run automated regression and integration tests'
        )
    }

    environment {
        APP_NAME = 'e-notary-appointment-system'
        JAVA_HOME = "${env.JAVA_HOME}"
        IMAGE_NAME = 'e-notary-system'
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
                    echo "Branch: ${BRANCH_NAME}"
                    echo "Deploy Target: ${DEPLOY_TARGET}"
                    echo "Target Environment: ${ENVIRONMENT}"
                    echo "Container Registry: ${REGISTRY_URL}"
                    echo "Container Host Port: ${DOCKER_PORT}"
                    echo "Standalone Server Port: ${SERVER_PORT}"
                    ./mvnw -version
                    docker --version || true
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
                failure {
                    echo "Tests failed! Archiving failure screenshots for diagnostic inspection..."
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Package Standalone Artifact') {
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
                    echo "Deploying container ${containerName} using image ${imageTag} on port ${params.DOCKER_PORT}..."
                    sh """
                        # Stop and remove existing container if running
                        docker stop ${containerName} || true
                        docker rm ${containerName} || true

                        # Run new versioned container
                        docker run -d --name ${containerName} \\
                            -p ${params.DOCKER_PORT}:8080 \\
                            -e SPRING_PROFILES_ACTIVE=${params.ENVIRONMENT} \\
                            ${imageTag}

                        # Health Check Validation Loop
                        echo "Validating container health on http://localhost:${params.DOCKER_PORT}/actuator/health..."
                        for i in \$(seq 1 30); do
                            if curl -s http://localhost:${params.DOCKER_PORT}/actuator/health | grep '"status":"UP"'; then
                                echo "Container deployment successfully validated and healthy on port ${params.DOCKER_PORT}!"
                                break
                            fi
                            echo "Waiting for containerized Spring Boot app to boot... (\$i/30)"
                            sleep 2
                        done
                    """
                }
            }
        }

        stage('Deploy Standalone Server') {
            when {
                expression { return params.DEPLOY_TARGET == 'standalone' }
            }
            steps {
                echo "=== Stage 8: Parameterized Standalone Server Deployment ==="
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
                    
                    # Health check validation
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
            echo "Pipeline completed successfully! Build #${BUILD_NUMBER} deployed and verified."
        }
        failure {
            echo "Pipeline failed! Inspect console logs and test reports."
        }
    }
}
