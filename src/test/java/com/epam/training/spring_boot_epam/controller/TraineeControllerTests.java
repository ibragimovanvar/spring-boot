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
@DisplayName("Integration tests for TraineeController API endpoints")
@Tag("Trainees")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TraineeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseTraineeUrl = "/v1/trainees";
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
    @Order(1)
    void getTraineeTrainings_WhenValid_ShouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTraineeUrl + "/trainings"))
                        .header("Authorization", "Bearer " + token)
                        .content(String.format("{\"username\": \"%s\"}", username))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        boolean resultSuccess = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(resultSuccess);

    }

    @Test
    @Order(2)
    void createTrainee_WhenValid_ShouldReturnCreated() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(String.format(baseTraineeUrl))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"firstName\": \"%s\", \"lastName\": \"%s\", \"birthDate\": \"%s\"}", "TraineeTest", "IbragimovTest", "2004-10-10")))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String resultUsername = jsonNode.get("data").get("username").asText();
        String resultPassword = jsonNode.get("data").get("password").asText();

        Assertions.assertNotNull(resultUsername);
        Assertions.assertNotNull(resultPassword);
    }

    @Test
    @Order(3)
    void getTraineeByUsername_WhenAuthenticated_ShouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTraineeUrl + "/%s", username))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        boolean success = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(success);
    }

    @Test
    @Order(4)
    void getTraineeByUsername_WhenNotAuthenticated_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTraineeUrl + "/%s", username))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    @Order(5)
    void updateTrainee_WhenAuthenticated_ShouldReturnAccepted() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put(String.format(baseTraineeUrl))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": \"Anvar Updated\", " +
                                "\"lastName\": \"Ibragimov 2\", " +
                                "\"address\": \"Some address\", " +
                                "\"active\": false, " +
                                "\"birthDate\": \"2004-10-10\"}"))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String resultFirstName = jsonNode.get("data").get("firstName").asText();
        String resultLastName = jsonNode.get("data").get("lastName").asText();

        Assertions.assertEquals("Anvar Updated", resultFirstName);
        Assertions.assertEquals("Ibragimov 2", resultLastName);
    }

    @Test
    @Order(99)
    void deleteTrainee_WhenAuthenticated_ShouldReturnNoContent() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .delete(String.format(baseTraineeUrl + "/delete"))
                        .param("username", username)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        boolean resultSuccess = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(resultSuccess);
    }

    @Test
    @Order(98)
    void activateOrDeactivate_WhenAuthenticated_ShouldReturnOk() throws Exception {
        boolean active = false;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .patch(String.format(baseTraineeUrl))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\": \"%s\", \"active\": %s}", username, active)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        boolean resultSuccess = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(resultSuccess);
    }

    @Test
    @Order(6)
    void getNotAssignedActiveTrainers_WhenAuthenticated_ShouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTraineeUrl + "/not-assigned"))
                        .param("username", username)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        boolean resultSuccess = jsonNode.get("success").asBoolean();

        Assertions.assertTrue(resultSuccess);
    }
}
