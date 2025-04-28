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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for TrainingTypeController API endpoints")
@Tag("Training Types")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainingTypeControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @Autowired
    private TraineeService traineeService;

    private String username;
    private String password;

    @BeforeAll
    void setUp() throws Exception {
        if (username == null || password == null || token == null) {
            ApiResponse<AuthDTO> profile = traineeService.createProfile(new TraineeCreateDTO("Test", "User", "123", LocalDate.now()));
            this.password = profile.getData().getPassword();
            this.username = profile.getData().getUsername();
        }

        String baseAuthUrl = "/v1/auth";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(baseAuthUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password)
                        ))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        token = jsonNode.get("data").get("token").asText();
    }

    @Test
    void getTrainingTypes_WhenTypesExist_ShouldReturnOk() throws Exception {
        String baseTrainingTypesUrl = "/v1/training-types";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(baseTrainingTypesUrl)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        boolean resultSuccess = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(resultSuccess);
        Assertions.assertNotNull(jsonNode.get("data"));
    }
}