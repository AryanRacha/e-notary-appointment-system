# Stage 10 — Continuous Testing & Automated Quality Gates in Jenkins

## 1. Overview & Pedagogical Objectives
Continuous Testing (CT) transforms automated testing from a passive verification step into an active **Quality Gate** within the CI/CD pipeline. 

In this stage, we established a fail-fast feedback loop in our declarative Jenkins pipeline:
1. **Automated Test Reporting Quality Gate**: Integrates JUnit test reporting (`junit testResults: 'target/surefire-reports/*.xml'`) and failure artifact archiving (`target/screenshots/*.png`) directly into the pipeline test stage.
2. **Intentional Defect Injection**: Created a feature branch (`feature/intentional-defect`) and injected a breaking regression into the reference code generation algorithm.
3. **Pipeline Failure & Deployment Blockade**: Demonstrated that test assertions halt the pipeline immediately at Stage 4 ("Automated Testing"), automatically marking the build as `FAILURE`, archiving diagnostics and failure screenshots, and strictly skipping downstream packaging and deployment.
4. **Defect Remediation & Verification**: Corrected the defect, committed the fix, and verified that Jenkins re-evaluates the pipeline to a green state (`SUCCESS`), deploying the validated application to the server.

For Python/TypeScript developers, this is equivalent to configuring GitHub Actions / GitLab CI with `pytest` / `vitest` reporters where downstream deployment jobs specify `needs: [test]` and fail-fast policies abort before AWS / ECS deployment triggers.

---

## 2. Quality Gate Pipeline Architecture

```
                                    +-----------------------+
                                    |   Code Commit (Git)   |
                                    +-----------------------+
                                                |
                                                v
                                    +-----------------------+
                                    |    Stage 1: Checkout  |
                                    +-----------------------+
                                                |
                                                v
                                    +-----------------------+
                                    | Stage 2: Environment  |
                                    +-----------------------+
                                                |
                                                v
                                    +-----------------------+
                                    |    Stage 3: Compile   |
                                    +-----------------------+
                                                |
                                                v
                                    +-----------------------+
                                    | Stage 4: Auto Testing |
                                    |   (JUnit 5 + Selenium)|
                                    +-----------------------+
                                        /               \
                          [Assertions Pass]        [Assertion Fails]
                                      /                   \
                                     v                     v
                  +---------------------+       +-----------------------+
                  |  Stage 5: Packaging |       |  Post: Archive XML    |
                  +---------------------+       |  Post: Archive .PNG   |
                             |                  +-----------------------+
                             v                             |
                  +---------------------+                  v
                  |  Stage 6: Deploy    |       +-----------------------+
                  +---------------------+       |  ABORT PIPELINE       |
                             |                  |  (Skip Pack & Deploy) |
                             v                  +-----------------------+
                  +---------------------+
                  | Health Check Valid  |
                  +---------------------+
```

---

## 3. Implementation Details

### 3.1 Declarative Pipeline Quality Gate (`Jenkinsfile`)
The testing stage incorporates post-action conditions to record test reports and conditionally capture UI failure screenshots:
```groovy
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
```

### 3.2 Intentional Defect Injection
To test the quality gate, we branched to `feature/intentional-defect` and modified `BookingService.java`:
```java
// INTENTIONAL DEFECT: Altered standardized prefix from 'ENOT-' to 'DEFECT-'
StringBuilder sb = new StringBuilder("DEFECT-" + year + "-");
```

This intentional defect broke the domain invariant, causing:
1. `BookingServiceTest.createBooking_Success_GeneratesReferenceCodeAndMarksSlotBooked` to fail assertion `refCode.startsWith("ENOT-")`.
2. `ENotaryE2ETest.test03_clientRegistrationAndBooking` to fail in headless Chromium when validating the generated booking reference code on the confirmation screen.
3. `ScreenshotTestWatcher` to trigger and capture the live UI state into `target/screenshots/`.

---

## 4. Execution Evidence & Quality Gate Enforcement

### 4.1 Jenkins Build #6 (Defective Branch — Quality Gate Tripped)
- **Branch**: `feature/intentional-defect`
- **Build Result**: `FAILURE` (Total time: 40.6s)
- **Surefire Test Results**: 39 Tests run, 2 Failures, 0 Errors.
```
[ERROR] Failures: 
[ERROR]   ENotaryE2ETest.test03_clientRegistrationAndBooking:153 Booking reference code should follow ENOT-YYYY-XXXX pattern: DEFECT-2026-VZLR ==> expected: <true> but was: <false>
[ERROR]   BookingServiceTest.createBooking_Success_GeneratesReferenceCodeAndMarksSlotBooked:75 
Expecting actual:
  "DEFECT-2026-5XK4"
to start with:
  "ENOT-"

[Pipeline] junit
Recording test results
[Pipeline] echo
Tests failed! Archiving failure screenshots for diagnostic inspection...
[Pipeline] archiveArtifacts
Archiving artifacts
[Pipeline] stage
[Pipeline] { (Package Artifact)
Stage "Package Artifact" skipped due to earlier failure(s)
[Pipeline] stage
[Pipeline] { (Deploy to Server)
Stage "Deploy to Server" skipped due to earlier failure(s)
```
- **Archived Diagnostic Artifact**:
  - `ENotaryE2ETest_test03_clientRegistrationAndBooking_20261026_230526.png`
  - Displayed the confirmation card showing `BOOKING REFERENCE CODE: DEFECT-2026-VZLR`, directly pinpointing the defect root cause.

### 4.2 Defect Resolution & Jenkins Build #7 (Restored Green State)
- **Fix**: Restored `StringBuilder sb = new StringBuilder("ENOT-" + year + "-");` in `BookingService.java`.
- **Commit**: `cd002c3 fix(booking): restore standardized ENOT- reference code prefix`.
- **Build Result**: `SUCCESS`
```
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[Pipeline] junit
Recording test results
[Pipeline] stage (Package Artifact)
[INFO] Building jar: .../target/e-notary-appointment-system-1.0.0-SNAPSHOT.jar
[Pipeline] archiveArtifacts
Archiving artifacts
Recording fingerprints
[Pipeline] stage (Deploy to Server)
Deploying application to Tomcat embedded runtime on port 8080 in staging mode...
{"status":"UP","components":{"db":{"status":"UP"},"diskSpace":{"status":"UP"},"ping":{"status":"UP"}}}
Application successfully deployed and healthy on port 8080!
[Pipeline] End of Pipeline
Finished: SUCCESS
Completed e-notary-pipeline #7 : SUCCESS
```

---

## 5. Artifacts Produced & Updated
| Artifact | Purpose |
|---|---|
| `Jenkinsfile` | Declarative pipeline with JUnit report publisher and failure screenshot archival |
| `jenkins/pipeline-job-config.xml` | Pipeline XML parameterized with `BRANCH_NAME` |
| `target/screenshots/*.png` | Full 1080p browser failure captures automatically preserved by Jenkins |
| `docs/10-continuous-testing-in-jenkins.md` | Formal documentation of quality gate failure, diagnostics, and recovery |
