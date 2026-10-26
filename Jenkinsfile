pipeline {
    agent any

    parameters {
        string(
            name: 'BRANCH_NAME',
            defaultValue: 'develop',
            description: 'Git branch to checkout and build'
        )
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
        JAVA_HOME = "${env.JAVA_HOME}"
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
                failure {
                    echo "Tests failed! Archiving failure screenshots for diagnostic inspection..."
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
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
            echo "Pipeline completed successfully! Artifact packaged and deployed."
        }
        failure {
            echo "Pipeline failed! Inspect console logs and test reports."
        }
    }
}
