package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.request.AuthLoginRequest;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TokenResponse;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.security.service.AuthService;
import com.epam.training.spring_boot_epam.security.service.JwtService;
import com.epam.training.spring_boot_epam.security.service.impl.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthService authService;

    @MockBean
    private RateLimiter rateLimiter;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService; // Add this to mock CustomUserDetailsService

    @Autowired
    private ObjectMapper objectMapper;

    private AuthLoginRequest authDTO;
    private PasswordChangeRequest passwordChangeRequest;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        authDTO = new AuthLoginRequest("john_doe", "password123");
        passwordChangeRequest = new PasswordChangeRequest("john_doe", "password123", "newPassword456");

        // Create user
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword(passwordEncoder.encode("password123"));
        userDao.save(user);

        // Login to get token
        MvcResult result = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andReturn();

        jwtToken = objectMapper.readValue(result.getResponse().getContentAsString(), ApiResponse<TokenResponse>.class)
                .getData().getToken;
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnSuccess() throws Exception {
        when(rateLimiter.acquirePermission()).thenReturn(true);
        ApiResponse<TokenResponse> response = new ApiResponse<>(true, "You logged in successfully",
                new TokenResponse("jwt_token", 3600L));
        when(authService.login(any(AuthLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("You logged in successfully"))
                .andExpect(jsonPath("$.data.jwt").value("jwt_token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));

        verify(authService, times(1)).login(any(AuthLoginRequest.class));
    }

    @Test
    void login_WhenRateLimitExceeded_ShouldReturnTooManyRequests() throws Exception {
        when(rateLimiter.acquirePermission()).thenReturn(false);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isTooManyRequests()) // 429
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again after 5 minutes."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService, never()).login(any(AuthLoginRequest.class));
    }

    @Test
    void changePassword_WhenRequestIsValid_ShouldReturnSuccess() throws Exception {
        when(userDao.updatePassword("john_doe", "password123", "newPassword456")).thenReturn(true);

        mockMvc.perform(put("/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userDao, times(1)).updatePassword("john_doe", "password123", "newPassword456");
    }

    @Test
    void changePassword_WhenRequestIsInvalid_ShouldReturnBadRequest() throws Exception {
        when(userDao.updatePassword("john_doe", "password123", "newPassword456")).thenReturn(false);

        mockMvc.perform(put("/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to change password. Please verify your username and old password."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userDao, times(1)).updatePassword("john_doe", "password123", "newPassword456");
    }

    @Test
    void logout_WhenTokenIsValid_ShouldReturnSuccess() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, "You logged out successfully", null);
        when(authService.logout("jwt_token")).thenReturn(response);

        mockMvc.perform(post("/v1/auth/logout")
                        .header("Authorization", "Bearer jwt_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("You logged out successfully"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(authService, times(1)).logout("jwt_token");
    }
}