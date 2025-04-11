package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.service.TrainingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingController.class)
class TrainingControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingService trainingService;

    @Autowired
    private ObjectMapper objectMapper;

    private TrainingDTO trainingDTO;

    @BeforeEach
    void setUp() {

        trainingDTO = new TrainingDTO();
        trainingDTO.setTraineeUsername("john_doe");
        trainingDTO.setTrainerUsername("jane_smith");
        trainingDTO.setTrainingName("Morning Yoga");
        trainingDTO.setTrainingDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));
        trainingDTO.setTrainingDurationInMinutes(1);

        TraineeFilterResponseDTO traineeResponseDTO = new TraineeFilterResponseDTO();
        traineeResponseDTO.setId(1L);
        traineeResponseDTO.setTrainingName("Morning Yoga");
        traineeResponseDTO.setTrainingDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));
        traineeResponseDTO.setTrainingDurationInHours(1);
        traineeResponseDTO.setTrainerFirstname("Jane");
        traineeResponseDTO.setTrainingType("Yoga");

        TrainerFilterResponseDTO trainerResponseDTO = new TrainerFilterResponseDTO();
        trainerResponseDTO.setId(1L);
        trainerResponseDTO.setTrainingName("Morning Yoga");
        trainerResponseDTO.setTrainingDateTime(LocalDateTime.of(2023, 10, 1, 9, 0));
        trainerResponseDTO.setTrainingDurationInHours(1);
        trainerResponseDTO.setTraineeFirstname("John");
        trainerResponseDTO.setTrainingType("Yoga");
    }

    @Test
    void getTraineeTrainings_WhenInvalid_ShouldReturn_4xxClientError() throws Exception {
        mockMvc.perform(get("/v1/trainings/trainee-trainings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing header: username"));
    }

    @Test
    void getTrainerTrainings_WhenInvalid_ShouldReturn_4xxClientError() throws Exception {
        mockMvc.perform(get("/v1/trainings/trainer-trainings")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Missing header: username"));
    }


    @Test
    void createTraining_WhenValid_ShouldReturnCreated() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, null, null);
        when(trainingService.addTraining(any(TrainingDTO.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainingDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(trainingService, times(1)).addTraining(any(TrainingDTO.class));
    }
}