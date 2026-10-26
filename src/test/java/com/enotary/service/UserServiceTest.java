package com.enotary.service;

import com.enotary.model.Notary;
import com.enotary.model.Role;
import com.enotary.model.User;
import com.enotary.repository.NotaryRepository;
import com.enotary.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotaryRepository notaryRepository;

    @Autowired
    private com.enotary.repository.AppointmentSlotRepository slotRepository;

    @Autowired
    private com.enotary.repository.BookingRequestRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        slotRepository.deleteAll();
        notaryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerClient_Success() {
        User client = userService.registerClient("Alice Citizen", "alice@example.com", "secret123", "1234567890");

        assertThat(client.getId()).isNotNull();
        assertThat(client.getFullName()).isEqualTo("Alice Citizen");
        assertThat(client.getEmail()).isEqualTo("alice@example.com");
        assertThat(client.getRole()).isEqualTo(Role.CLIENT);
        assertThat(client.getPasswordHash()).isNotEqualTo("secret123"); // Verifies password was hashed
    }

    @Test
    void registerClient_DuplicateEmail_ThrowsException() {
        userService.registerClient("Alice Citizen", "alice@example.com", "secret123", "1234567890");

        assertThatThrownBy(() -> userService.registerClient("Alice Clone", "alice@example.com", "password999", "9876543210"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void registerNotary_Success() {
        Notary notary = userService.registerNotary(
                "Bob Notary",
                "bob@notary.com",
                "notarypass",
                "555-1111",
                "NOT-COMM-99",
                "100 Court St",
                "Affidavits & Wills"
        );

        assertThat(notary.getId()).isNotNull();
        assertThat(notary.getLicenseNumber()).isEqualTo("NOT-COMM-99");
        assertThat(notary.getUser().getRole()).isEqualTo(Role.NOTARY);
        assertThat(notary.getUser().getEmail()).isEqualTo("bob@notary.com");
    }

    @Test
    void registerNotary_DuplicateLicense_ThrowsException() {
        userService.registerNotary(
                "Bob Notary", "bob@notary.com", "pass123", "555-1111",
                "NOT-COMM-99", "100 Court St", "Affidavits"
        );

        assertThatThrownBy(() -> userService.registerNotary(
                "Charlie Notary", "charlie@notary.com", "pass456", "555-2222",
                "NOT-COMM-99", "200 Market St", "Deeds"
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("already exists");
    }

    @Test
    void authenticate_ValidCredentials_ReturnsUser() {
        userService.registerClient("Alice Citizen", "alice@example.com", "correctpassword", "1234567890");

        Optional<User> result = userService.authenticate("alice@example.com", "correctpassword");

        assertThat(result).isPresent();
        assertThat(result.get().getFullName()).isEqualTo("Alice Citizen");
    }

    @Test
    void authenticate_InvalidPassword_ReturnsEmpty() {
        userService.registerClient("Alice Citizen", "alice@example.com", "correctpassword", "1234567890");

        Optional<User> result = userService.authenticate("alice@example.com", "wrongpassword");

        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_NonExistentEmail_ReturnsEmpty() {
        Optional<User> result = userService.authenticate("nobody@example.com", "anypassword");

        assertThat(result).isEmpty();
    }
}
