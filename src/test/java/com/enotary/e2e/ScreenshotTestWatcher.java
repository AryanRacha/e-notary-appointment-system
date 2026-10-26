package com.enotary.e2e;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * JUnit 5 extension that automatically captures and persists
 * failure screenshots whenever an E2E Selenium test assertion fails,
 * and subsequently disposes of the WebDriver session.
 */
public class ScreenshotTestWatcher implements AfterEachCallback {

    private static final String SCREENSHOT_DIR = "target/screenshots";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public void afterEach(ExtensionContext context) {
        Object testInstance = context.getTestInstance().orElse(null);
        if (testInstance instanceof BaseE2ETest) {
            BaseE2ETest base = (BaseE2ETest) testInstance;
            WebDriver driver = base.getDriver();

            if (context.getExecutionException().isPresent()) {
                Throwable cause = context.getExecutionException().get();
                String testMethodName = context.getRequiredTestMethod().getName();
                String testClassName = context.getRequiredTestClass().getSimpleName();
                String timestamp = LocalDateTime.now().format(FORMATTER);

                System.err.println(">>> [SELENIUM] Test Failed: " + testClassName + "." + testMethodName);
                System.err.println(">>> [SELENIUM] Failure Reason: " + cause.getMessage());

                if (driver instanceof TakesScreenshot) {
                    try {
                        Path dirPath = Paths.get(SCREENSHOT_DIR);
                        if (!Files.exists(dirPath)) {
                            Files.createDirectories(dirPath);
                        }

                        File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                        String screenshotFilename = String.format("%s_%s_%s.png", testClassName, testMethodName, timestamp);
                        Path destPath = dirPath.resolve(screenshotFilename);
                        Files.copy(srcFile.toPath(), destPath);

                        System.err.println(">>> [SELENIUM] Captured failure screenshot to: " + destPath.toAbsolutePath());
                    } catch (Exception e) {
                        System.err.println(">>> [SELENIUM] Failed to capture screenshot: " + e.getMessage());
                    }
                }
            }

            // Safely quit driver after screenshot capture
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignored) {}
            }
        }
    }
}
