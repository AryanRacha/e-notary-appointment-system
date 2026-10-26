package com.enotary.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.time.Duration;

/**
 * Base class for all Selenium WebDriver End-to-End test suites.
 * Starts an in-process Spring Boot application on a dynamic port,
 * configures headless Chromium, attaches the ScreenshotTestWatcher extension,
 * and provides retry helpers resilient to React virtual DOM reconciliation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ExtendWith(ScreenshotTestWatcher.class)
public abstract class BaseE2ETest {

    @LocalServerPort
    protected int port;

    protected WebDriver driver;
    protected String baseUrl;
    protected WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        this.baseUrl = "http://localhost:" + port;

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");

        if (new File("/usr/bin/chromium").exists()) {
            options.setBinary("/usr/bin/chromium");
        }
        if (new File("/usr/bin/chromedriver").exists()) {
            System.setProperty("webdriver.chrome.driver", "/usr/bin/chromedriver");
        }

        this.driver = new ChromeDriver(options);
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public WebDriver getDriver() {
        return driver;
    }

    protected WebElement waitFor(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected void click(By locator) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement el = waitForClickable(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true); arguments[0].click();", el);
                return;
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
    }

    protected void type(By locator, String text) {
        for (int i = 0; i < 3; i++) {
            try {
                WebElement el = waitFor(locator);
                el.clear();
                el.sendKeys(text);
                return;
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
    }

    protected String getText(By locator) {
        for (int i = 0; i < 3; i++) {
            try {
                return waitFor(locator).getText();
            } catch (StaleElementReferenceException e) {
                if (i == 2) throw e;
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
        return "";
    }
}
