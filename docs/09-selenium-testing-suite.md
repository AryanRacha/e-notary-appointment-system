# Stage 09 — Selenium Testing Suite & Automated Failure Screenshots

## 1. Overview & Pedagogical Objectives
In this stage, we implemented an automated end-to-end (E2E) browser testing suite using **Selenium WebDriver** and **JUnit 5**, fully integrated into the standard Maven build lifecycle (`./mvnw test`). 

For developers coming from TypeScript/Python ecosystems (e.g., Playwright, Cypress, Puppeteer, or pytest-selenium), Selenium in Java with JUnit 5 operates on similar core browser-automation principles:
- **Headless Browser Execution**: Uses Chromium in `--headless=new` mode with `--no-sandbox` and `--disable-dev-shm-usage` for resource efficiency in Linux server and CI environments.
- **Dynamic Port Binding**: Integrates with `@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)` so tests boot the full application stack on an ephemeral port, eliminating port conflicts.
- **JUnit 5 Extension Lifecycle**: Implements `AfterEachCallback` to automatically inspect the test execution context and capture full-screen PNG screenshots immediately upon assertion failures or timeouts before terminating the browser instance.
- **DOM Stability & Stale Element Handling**: Implements resilient helper wrappers with retry logic to gracefully accommodate asynchronous React DOM reconciliation cycles.

---

## 2. Architecture & Design

### 2.1 Component Interaction
```
+--------------------------------------------------------------------------+
|                        JUnit 5 Test Runner                                |
|  +--------------------------------------------------------------------+  |
|  | ScreenshotTestWatcher (implements AfterEachCallback)              |  |
|  | - Listens for test failures / exceptions                            |  |
|  | - Takes 1080p PNG screenshot to target/screenshots/                 |  |
|  +--------------------------------------------------------------------+  |
|                                   |                                      |
|  +--------------------------------------------------------------------+  |
|  | BaseE2ETest                                                        |  |
|  | - Configures ChromeDriver & Headless Chromium Options              |  |
|  | - StaleElementReferenceException retry helpers (click, type, wait) |  |
|  +--------------------------------------------------------------------+  |
|                                   |                                      |
|  +--------------------------------------------------------------------+  |
|  | ENotaryE2ETest (@TestMethodOrder(MethodOrderer.OrderAnnotation))   |  |
|  | 1. test01_publicPortalAndSlotsDisplay                              |  |
|  | 2. test02_loginAndRoleRedirection                                  |  |
|  | 3. test03_clientRegistrationAndBooking                             |  |
|  | 4. test04_statusTrackingAndCancellation                            |  |
|  +--------------------------------------------------------------------+  |
+--------------------------------------------------------------------------+
                                    |
                            HTTP / DOM Protocol
                                    v
+--------------------------------------------------------------------------+
|  Spring Boot Embedded Tomcat (:randomPort)                                |
|  - Forwarding: GET / -> /index.html (React Vite SPA)                      |
|  - REST APIs: /api/auth/*, /api/slots/*, /api/bookings/*, /api/notaries   |
|  - In-Memory H2 Database                                                 |
+--------------------------------------------------------------------------+
```

---

## 3. Implementation Details

### 3.1 Failure Screenshot Extension (`ScreenshotTestWatcher.java`)
In JUnit 5, `@AfterEach` methods execute *before* standard `TestWatcher.testFailed()` callbacks. Quitting the driver in `@AfterEach` would close the browser session before a screenshot could be captured.
To solve this, `ScreenshotTestWatcher` implements `AfterEachCallback`:
```java
@Override
public void afterEach(ExtensionContext context) {
    if (context.getExecutionException().isPresent()) {
        captureScreenshot(context);
    }
}
```
If an unhandled exception occurred, it invokes Selenium's `TakesScreenshot` interface:
```java
byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
Files.write(screenshotPath, screenshotBytes);
```
Screenshots are timestamped and organized into `target/screenshots/`.

### 3.2 Base Test Runner (`BaseE2ETest.java`)
Configures the headless browser environment:
- Explicit binary location: `/usr/bin/chromium`
- Headless driver: `/usr/bin/chromedriver`
- Arguments: `--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`, `--window-size=1920,1080`
- Explicit wait wrappers (`WebDriverWait(driver, Duration.ofSeconds(10))`) with 3-attempt retry handlers for `StaleElementReferenceException`.

### 3.3 End-to-End User Scenarios (`ENotaryE2ETest.java`)
The suite evaluates 4 core real-world user journeys sequentially:
1. **Journey 1: Public Portal & Slots Display**
   - Navigates to `/`.
   - Validates the navbar, brand headers, and public notaries list.
   - Clicks "Find Slots", selects a notary, and asserts discrete time slots are visible.
2. **Journey 2: Authentication & Role Redirection**
   - Logs in with Notary credentials (`jane.notary@example.com`).
   - Asserts role-specific redirection to the Notary Dashboard.
   - Verifies the Schedule Manager and Appointment Management tables render.
   - Logs out cleanly and returns to public state.
3. **Journey 3: Client Registration & Slot Booking**
   - Registers a new client with dynamic email (`e2e_client_<timestamp>@test.org`).
   - Signs into the new account.
   - Selects an available slot and submits a notarization request.
   - Validates generation of an official reference code (`ENOT-YYYY-XXXX`).
4. **Journey 4: Status Tracking & Cancellation**
   - Uses the reference code from Journey 3 in the public Tracking portal.
   - Verifies initial status is `PENDING`.
   - Simulates client cancellation with a documented cancellation reason.
   - Asserts immediate status update to `CANCELLED`.

---

## 4. Verification & Execution Evidence

### 4.1 Maven Build & Test Suite Output
Command:
```bash
./mvnw test
```
Execution log:
```
[INFO] Running com.enotary.e2e.ENotaryE2ETest
2026-10-26T22:56:59.303+05:30  INFO Tomcat started on port 46881 (http) with context path ''
>>> Generated E2E Booking Reference: ENOT-2026-8ASR
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.59 s -- in com.enotary.e2e.ENotaryE2ETest
[INFO] Running com.enotary.ENotaryApplicationTests
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.093 s -- in com.enotary.ENotaryApplicationTests
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 39, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  37.282 s
```

### 4.2 Automated Screenshot Proof on Failure
When an assertion fails (such as an unmounted form or misconfigured route), the test watcher captures an exact 1080p PNG snapshot into `target/screenshots/`:
- Artifact: `target/screenshots/ENotaryE2ETest_test03_clientRegistrationAndBooking_20261026_225334.png`
- File properties: PNG image data, 1920 x 1080, 8-bit/color RGB, non-interlaced.

---

## 5. Artifacts Produced
| File | Description |
|---|---|
| `src/test/java/com/enotary/e2e/BaseE2ETest.java` | Base test class initializing headless Chromium and retry utilities |
| `src/test/java/com/enotary/e2e/ScreenshotTestWatcher.java` | JUnit 5 failure listener producing 1080p full-page PNG screenshots |
| `src/test/java/com/enotary/e2e/ENotaryE2ETest.java` | 4 end-to-end browser user scenarios |
| `src/main/java/com/enotary/controller/NotaryRestController.java` | REST endpoint `/api/notaries` providing notary metadata to frontend |
| `frontend/src/pages/Register.jsx` | Fixed registration redirect lifecycle with `useEffect` unmount cleanup |
