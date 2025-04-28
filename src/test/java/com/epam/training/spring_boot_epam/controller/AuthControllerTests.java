package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for AuthController API endpoints")
@Tag("Auth")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseAuthUrl = "/v1/auth";
    private String token;
    private Long expiresIn;

    @Autowired
    private TraineeService traineeService;

    private String username;
    private String password;

    @BeforeAll
    void setUp() throws Exception {
        if (username == null || password == null) {
            ApiResponse<AuthDTO> profile = traineeService.createProfile(new TraineeCreateDTO("Pro", "User", "123", LocalDate.now()));
            this.password = profile.getData().getPassword();
            this.username = profile.getData().getUsername();
        }

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(baseAuthUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        token = jsonNode.get("data").get("token").asText();
        expiresIn = jsonNode.get("data").get("expiresIn").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("/login (POST) -> SUCCESS")
    void loginEndpoint_ShouldReturnSuccess() {
        Assertions.assertNotNull(token);
        Assertions.assertEquals(3600L, expiresIn);
    }


    @Test
    @Order(1)
    @DisplayName("/logout (POST) -> SUCCESS")
    void logoutEndpoint_ShouldReturnSuccess() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(baseAuthUrl + "/logout")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Assertions.assertTrue(jsonNode.get("success").asBoolean(), "Response should indicate success");
        Assertions.assertNotNull(jsonNode.get("message").asText(), "Message should not be null");
    }

    @Test
    @Order(3)
    @DisplayName("/login (POST more than 5 times unsuccessful) -> FAILED")
    void loginEndpoint_ShouldReturnTooManyRequests() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(MockMvcRequestBuilders
                    .post(baseAuthUrl + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password)));
        }

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                        .post(baseAuthUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password)))
                .andExpect(status().isTooManyRequests())
                .andReturn();

        Assertions.assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), mvcResult.getResponse().getStatus());
    }
}