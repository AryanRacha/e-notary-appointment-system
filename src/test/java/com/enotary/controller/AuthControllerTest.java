package com.enotary.controller;

import com.enotary.model.User;
import com.enotary.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    void showRegisterForm_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("Create an Account")));
    }

    @Test
    void processRegistration_ValidClient_RedirectsToLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("fullName", "Test User")
                        .param("email", "testuser@example.com")
                        .param("password", "password123")
                        .param("phone", "555-0199")
                        .param("role", "CLIENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        assertThat(userService.findByEmail("testuser@example.com")).isPresent();
    }

    @Test
    void processRegistration_DuplicateEmail_ShowsErrorMessage() throws Exception {
        userService.registerClient("Existing User", "duplicate@example.com", "pass123", "555-0000");

        mockMvc.perform(post("/register")
                        .param("fullName", "Second User")
                        .param("email", "duplicate@example.com")
                        .param("password", "pass456")
                        .param("role", "CLIENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("already exists")));
    }

    @Test
    void showLoginForm_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Sign In")));
    }

    @Test
    void processLogin_ValidCredentials_SetsSessionAndRedirects() throws Exception {
        userService.registerClient("Sign In User", "signin@example.com", "correctpass", "555-1212");

        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/login")
                        .session(session)
                        .param("email", "signin@example.com")
                        .param("password", "correctpass"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?loginSuccess=true"));

        User sessionUser = (User) session.getAttribute("currentUser");
        assertThat(sessionUser).isNotNull();
        assertThat(sessionUser.getEmail()).isEqualTo("signin@example.com");
    }

    @Test
    void processLogin_InvalidCredentials_RendersLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "nonexistent@example.com")
                        .param("password", "badpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(containsString("Invalid email or password.")));
    }

    @Test
    void logout_InvalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("currentUser", new User("Temp", "temp@example.com", "hash", "123", null));

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        assertThat(session.isInvalid()).isTrue();
    }
}
