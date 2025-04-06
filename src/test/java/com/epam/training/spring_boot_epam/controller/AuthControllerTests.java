package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDao userDao;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthDTO authDTO;
    private PasswordChangeRequest passwordChangeRequest;

    @BeforeEach
    void setUp() {
        authDTO = new AuthDTO("john_doe", "password123");
        passwordChangeRequest = new PasswordChangeRequest("john_doe", "password123", "newPassword456");
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnSuccess() throws Exception {
        when(userDao.existsByUsernameAndPassword("john_doe", "password123")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login Successful"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userDao, times(1)).existsByUsernameAndPassword("john_doe", "password123");
    }

    @Test
    void login_WhenCredentialsAreInvalid_ShouldReturnUnauthorized() throws Exception {
        when(userDao.existsByUsernameAndPassword("john_doe", "password123")).thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Login or password wrong!"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userDao, times(1)).existsByUsernameAndPassword("john_doe", "password123");
    }

    @Test
    void changePassword_WhenRequestIsValid_ShouldReturnSuccess() throws Exception {
        when(userDao.updatePassword("john_doe", "password123", "newPassword456")).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/auth/change-password")
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

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to change password. Please verify your username and old password."))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userDao, times(1)).updatePassword("john_doe", "password123", "newPassword456");
    }
}