package com.enotary.e2e;

import com.enotary.model.BookingRequest;
import com.enotary.model.User;
import com.enotary.service.BookingService;
import com.enotary.service.SlotService;
import com.enotary.service.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End Selenium WebDriver test suite covering 4 critical user journeys:
 * 1. Public Portal & Available Slots Browse
 * 2. User Authentication (Negative & Positive Login + Role Verification)
 * 3. Client Registration & Appointment Booking
 * 4. Booking Status Tracking & Cancellation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ENotaryE2ETest extends BaseE2ETest {

    @Autowired
    private UserService userService;

    @Autowired
    private SlotService slotService;

    @Autowired
    private BookingService bookingService;

    private static String capturedBookingReference;

    @Test
    @Order(1)
    public void test01_publicPortalAndAvailableSlots() {
        driver.get(baseUrl + "/");

        // Verify Hero Banner & Title
        WebElement brand = waitFor(By.id("nav-brand"));
        assertTrue(brand.getText().contains("E-Notary"), "Brand title should contain E-Notary");

        WebElement bookBtn = waitFor(By.id("hero-btn-book"));
        assertTrue(bookBtn.isDisplayed(), "Hero Book Appointment button should be visible");

        // Navigate to Slots View
        click(By.id("hero-btn-book"));

        // Verify Notary Selection and Slot Cards
        waitFor(By.id("select-notary"));

        // Wait for async slot fetch to populate book slot buttons
        WebElement firstBookBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("button[id^='btn-book-slot-']")));
        assertNotNull(firstBookBtn, "At least one slot button should be rendered");

        List<WebElement> bookSlotBtns = driver.findElements(By.cssSelector("button[id^='btn-book-slot-']"));
        assertFalse(bookSlotBtns.isEmpty(), "Book Slot buttons should be available on active slots");
    }

    @Test
    @Order(2)
    public void test02_userAuthentication() {
        driver.get(baseUrl + "/");

        // Navigate to Login page
        click(By.id("btn-login"));
        waitFor(By.id("form-login"));

        // Step A: Negative Test - Invalid Credentials
        type(By.id("input-login-email"), "nonexistent.user@example.com");
        type(By.id("input-login-password"), "wrongpassword");
        click(By.id("btn-submit-login"));

        WebElement errorBanner = waitFor(By.id("login-error-banner"));
        assertTrue(errorBanner.getText().toLowerCase().contains("invalid"),
                "Error banner should notify about invalid credentials");

        // Step B: Positive Test - Valid Notary Credentials
        type(By.id("input-login-email"), "sarah.jenkins@notary.org");
        type(By.id("input-login-password"), "notary123");
        click(By.id("btn-submit-login"));

        // Verify Notary Portal Redirect & Greeting
        WebElement greeting = waitFor(By.id("user-greeting"));
        assertTrue(greeting.getText().contains("Sarah"), "Greeting should display logged-in notary's name");

        WebElement notaryNav = waitFor(By.id("nav-notary-dashboard"));
        assertTrue(notaryNav.isDisplayed(), "Notary Dashboard navigation link should be active");

        // Logout
        click(By.id("btn-logout"));
        waitFor(By.id("btn-login"));
    }

    @Test
    @Order(3)
    public void test03_clientRegistrationAndBooking() {
        driver.get(baseUrl + "/");

        // Register new Client
        click(By.id("btn-register"));
        waitFor(By.id("form-register"));

        long uniqueId = System.currentTimeMillis();
        String clientEmail = "e2e_client_" + uniqueId + "@test.org";

        type(By.id("input-fullName"), "E2E Automated Client");
        type(By.id("input-email"), clientEmail);
        type(By.id("input-phone"), "+1-555-7788");
        type(By.id("input-password"), "securepass123");
        click(By.id("btn-submit-register"));

        // Verify Registration Success
        WebElement regSuccess = waitFor(By.id("register-success-banner"));
        assertTrue(regSuccess.isDisplayed(), "Registration success banner should display");

        // Login as newly created Client
        click(By.id("link-goto-login"));
        waitFor(By.id("form-login"));

        type(By.id("input-login-email"), clientEmail);
        type(By.id("input-login-password"), "securepass123");
        click(By.id("btn-submit-login"));

        waitFor(By.id("user-greeting"));

        // Navigate to Browse Slots
        click(By.id("nav-slots"));
        waitFor(By.id("select-notary"));

        // Select first available slot
        click(By.cssSelector("button[id^='btn-book-slot-']"));

        // Fill Booking Request Form
        waitFor(By.id("form-booking"));
        type(By.id("input-notes"), "Automated Selenium E2E appointment request for Power of Attorney.");
        click(By.id("btn-submit-booking"));

        // Verify Booking Confirmation & Reference Code
        WebElement refCodeEl = waitFor(By.id("booking-reference-code"));
        String refCode = refCodeEl.getText().trim();
        assertNotNull(refCode, "Booking reference code must not be null");
        assertTrue(refCode.startsWith("ENOT-"), "Booking reference code should follow ENOT-YYYY-XXXX pattern: " + refCode);

        // Store reference code for Stage 4 tracking test
        capturedBookingReference = refCode;
        System.out.println(">>> Generated E2E Booking Reference: " + capturedBookingReference);
    }

    @Test
    @Order(4)
    public void test04_statusTrackingAndCancellation() {
        // Fallback: If test 3 didn't store a reference, create a seed booking directly
        if (capturedBookingReference == null) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            Long notaryId = userService.getAllNotaries().get(0).getId();
            var slots = slotService.getAvailableSlots(notaryId, tomorrow);
            assertFalse(slots.isEmpty(), "Slots must be available for fallback test");
            User demoClient = userService.findByEmail("john.citizen@example.com").orElse(null);
            assertNotNull(demoClient, "Demo client must exist");

            BookingRequest req = bookingService.createBooking(
                    demoClient,
                    slots.get(0).getId(),
                    "AFFIDAVIT",
                    "Automated fallback booking for status tracking"
            );
            capturedBookingReference = req.getReferenceCode();
        }

        driver.get(baseUrl + "/");

        // Navigate to Public Status Tracking Page
        click(By.id("nav-track"));
        waitFor(By.id("form-track"));

        // Search for the booking reference code
        type(By.id("input-track-code"), capturedBookingReference);
        click(By.id("btn-track-submit"));

        // Assert Tracking Card is populated
        WebElement resultCard = waitFor(By.id("tracking-result-card"));
        assertTrue(resultCard.isDisplayed(), "Tracking result card must be displayed");

        WebElement resultRef = waitFor(By.id("track-result-ref"));
        assertEquals(capturedBookingReference, resultRef.getText().trim(),
                "Displayed reference must match queried code");

        // Perform cancellation flow
        WebElement showCancelBtn = waitFor(By.id("btn-show-cancel"));
        click(By.id("btn-show-cancel"));

        waitFor(By.id("input-cancel-reason"));
        type(By.id("input-cancel-reason"), "Client rescheduling due to international travel schedule.");
        click(By.id("btn-confirm-cancel"));

        // Assert Status transitions to CANCELLED
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("track-result-status"), "CANCELLED"));
        WebElement statusEl = waitFor(By.id("track-result-status"));
        assertTrue(statusEl.getText().contains("CANCELLED"), "Booking status should update to CANCELLED");
    }
}
