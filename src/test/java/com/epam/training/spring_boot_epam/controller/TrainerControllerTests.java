package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.repository.TrainingTypeDao;
import com.epam.training.spring_boot_epam.service.TrainerService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Integration tests for TrainerController API endpoints")
@Tag("Trainers")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrainerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String baseTrainerUrl = "/v1/trainers";
    private String token;

    @Autowired
    private TrainerService trainerService;

    private String username;
    private String password;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private DataSource dataSource;

    @BeforeAll
    void init() throws Exception {
        List<TrainingType> trainingTypes = trainingTypeDao.findAll();
        if (trainingTypes.isEmpty()) {
            try (Connection connection = dataSource.getConnection()) {
                ScriptUtils.executeSqlScript(connection, new ClassPathResource("import/initial.sql"));
            }
        }

        if (username == null || password == null || token == null) {
            ApiResponse<AuthDTO> profile = trainerService.createProfile(new TrainerCreateDTO("Test", "User", 2L));
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
    void getTrainerTrainings_WhenValid_ShouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTrainerUrl + "/trainings"))
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
    void createTrainer_WhenValid_ShouldReturnCreated() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .post(String.format(baseTrainerUrl))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"firstName\": \"%s\", \"lastName\": \"%s\", \"trainingTypeId\": \"%s\"}", "TraineeTest", "IbragimovTest", "2")))
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
    void getTrainerByUsername_WhenAuthenticated_ShouldReturnOk() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTrainerUrl + "/%s", username))
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
    void getTrainerByUsername_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(String.format(baseTrainerUrl + "/%s", username))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andReturn();
    }

    @Test
    void updateTrainer_WhenAuthenticated_ShouldReturnAccepted() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .put(String.format(baseTrainerUrl))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\": \"%s\", \"firstName\": \"%s\", \"lastName\": \"%s\", \"active\": %s, \"trainingTypeId\": \"%s\"}",
                                username, "Updated", "IbragimovTest", true, "2")))
                .andExpect(status().isAccepted())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        String resultFirstName = jsonNode.get("data").get("firstName").asText();
        String resultLastName = jsonNode.get("data").get("lastName").asText();
        Boolean resultStatus = jsonNode.get("data").get("active").asBoolean();
        Long resultTrainingTypeId = jsonNode.get("data").get("trainingTypeId").asLong();

        Assertions.assertEquals("Updated", resultFirstName);
        Assertions.assertEquals("IbragimovTest", resultLastName);
        Assertions.assertEquals(true, resultStatus);
        Assertions.assertEquals(2L, resultTrainingTypeId);
    }

    @Test
    void deleteTrainer_WhenAuthenticated_ShouldReturnNoContent() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .delete(String.format(baseTrainerUrl + "/delete"))
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
    void activateOrDeactivate_WhenAuthenticated_ShouldReturnOk() throws Exception {
        boolean active = false;

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                        .patch(String.format(baseTrainerUrl))
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
}