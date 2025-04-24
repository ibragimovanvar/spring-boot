package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.epam.training.spring_boot_epam.service.TrainerService;
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
@DisplayName("Integration tests for TrainingController API endpoints")
@Tag("Training")
class TrainingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private TraineeService traineeService;

    private String traineeUsername;
    private String traineePassword;
    private String trainerUsername;
    private String trainerPassword;


    @BeforeEach
    void setUp() throws Exception {
        if (traineeUsername == null || traineePassword == null) {
            ApiResponse<AuthDTO> profile = traineeService.createProfile(new TraineeCreateDTO("Test", "User", "123", LocalDate.now()));
            this.traineePassword = profile.getData().getPassword();
            this.traineeUsername = profile.getData().getUsername();
        }
        if (trainerUsername == null || trainerPassword == null || token == null) {
            ApiResponse<AuthDTO> profile = trainerService.createProfile(new TrainerCreateDTO("Test", "User", 2L));
            this.trainerPassword = profile.getData().getPassword();
            this.trainerUsername = profile.getData().getUsername();
        }

        String baseAuthUrl = "/v1/auth";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(baseAuthUrl + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                String.format("{\"username\": \"%s\", \"password\": \"%s\"}", trainerUsername, trainerPassword)
                        ))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        token = jsonNode.get("data").get("token").asText();
    }

    @Test
    void createTraining_WhenValid_ShouldReturnCreated() throws Exception {
        String requestBody = String.format(
                "{ \"trainerUsername\": \"%s\", \"traineeUsername\": \"%s\", \"trainingName\": \"%s\", " +
                        "\"trainingDateTime\": \"%s\", \"trainingDurationInMinutes\": %d }",
                trainerUsername, traineeUsername, "new trainingName", "2024-02-25 00:00", 30);

        String baseTrainingUrl = "/v1/trainings";
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(String.format(baseTrainingUrl))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Boolean resultStatus = jsonNode.get("success").asBoolean();

        Assertions.assertEquals(true, resultStatus);
    }
}