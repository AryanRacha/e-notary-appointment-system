package com.enotary.controller;

import com.enotary.model.User;
import com.enotary.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerClient_ValidPayload_ReturnsCreated() throws Exception {
        Map<String, String> payload = Map.of(
                "fullName", "REST Client",
                "email", "restclient@example.com",
                "password", "secret123",
                "phone", "1234567890",
                "role", "CLIENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.user.email", is("restclient@example.com")));

        assertThat(userService.findByEmail("restclient@example.com")).isPresent();
    }

    @Test
    void registerClient_DuplicateEmail_ReturnsBadRequest() throws Exception {
        userService.registerClient("Existing", "dup@example.com", "pass123", "123");

        Map<String, String> payload = Map.of(
                "fullName", "Second",
                "email", "dup@example.com",
                "password", "pass123",
                "role", "CLIENT"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void login_ValidCredentials_ReturnsOkAndSetsSession() throws Exception {
        userService.registerClient("Login User", "login@example.com", "pass123", "123");

        MockHttpSession session = new MockHttpSession();
        Map<String, String> credentials = Map.of(
                "email", "login@example.com",
                "password", "pass123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.user.fullName", is("Login User")));

        User sessionUser = (User) session.getAttribute("currentUser");
        assertThat(sessionUser).isNotNull();
        assertThat(sessionUser.getEmail()).isEqualTo("login@example.com");
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        Map<String, String> credentials = Map.of(
                "email", "wrong@example.com",
                "password", "wrongpass"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(credentials)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Invalid email or password")));
    }

    @Test
    void getCurrentUser_WhenAuthenticated_ReturnsUser() throws Exception {
        User user = userService.registerClient("Current User", "current@example.com", "pass123", "123");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", user);

        mockMvc.perform(get("/api/auth/current").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authenticated", is(true)))
                .andExpect(jsonPath("$.user.email", is("current@example.com")));
    }

    @Test
    void logout_ClearsSession() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("Temp", "temp@example.com", "hash", "123", null));

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        assertThat(session.isInvalid()).isTrue();
    }
}
