# Stage 1: Multi-stage Builder using Eclipse Temurin JDK 17
FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /build

# Copy Maven Wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Pre-fetch Maven dependencies
RUN ./mvnw dependency:go-offline -B || true

# Copy frontend source and project source
COPY frontend/ frontend/
COPY src/ src/

# Build React SPA and package executable Spring Boot fat JAR
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Hardened, Minimal Production Runtime Container
FROM eclipse-temurin:17-jre-alpine AS runtime

# Metadata & Labels
LABEL maintainer="Aryan Racha (Roll No: 23102C0055)"
LABEL description="Containerized E-Notary Appointment System Production Container"
LABEL version="1.0.0"

WORKDIR /app

# Create unprivileged application user & group for container security hardening
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy packaged JAR artifact from builder stage
COPY --from=builder /build/target/e-notary-appointment-system-*.jar /app/app.jar

# Enforce secure file ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose Spring Boot HTTP service port
EXPOSE 8080

# Environment Configuration
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=production \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Native Container Healthcheck validating Actuator endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=25s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Launch the containerized application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
