package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.service.TraineeService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TraineeController.class)
class TraineeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingService trainingService;

    @MockBean
    private TraineeService traineeService;

    @Autowired
    private ObjectMapper objectMapper;

    private TraineeCreateDTO traineeCreateDTO;
    private TraineeDTO traineeDTO;
    private AuthDTO authDTO;
    private ActivateDeactiveRequest activateDeactiveRequest;

    @BeforeEach
    void setUp() {
        traineeCreateDTO = new TraineeCreateDTO("John", "Doe", "123 Main St", LocalDate.of(1990, 1, 1));
        traineeDTO = new TraineeDTO();
        traineeDTO.setFirstName("John");
        traineeDTO.setLastName("Doe");
        traineeDTO.setActive(true);
        authDTO = new AuthDTO("john_doe", "password123");
        activateDeactiveRequest = new ActivateDeactiveRequest("john_doe", true);
    }

    @Test
    void getTraineeTrainings_WhenValid_ShouldReturnOk() throws Exception {
        TraineeTrainingsFilter filter = new TraineeTrainingsFilter();
        filter.setTrainingTypeName("Yoga");

        TraineeFilterResponseDTO responseDTO = new TraineeFilterResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTrainingName("Morning Yoga");
        responseDTO.setTrainerFirstname("Jane");
        responseDTO.setTrainingType("Yoga");

        ApiResponse<List<TraineeFilterResponseDTO>> response =
                new ApiResponse<>(true, null, List.of(responseDTO));

        when(trainingService.getTraineeTrainings(eq("john_doe"), eq("password123"), any(TraineeTrainingsFilter.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainees/john_doe/trainings")
                        .header("username", "john_doe")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].trainingName").value("Morning Yoga"));
    }

    @Test
    void createTrainer_WhenValid_ShouldReturnCreated() throws Exception {
        ApiResponse<AuthDTO> response = new ApiResponse<>(true, null, authDTO);
        when(traineeService.createProfile(any(TraineeCreateDTO.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traineeCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("john_doe"));

        verify(traineeService, times(1)).createProfile(any(TraineeCreateDTO.class));
    }

    @Test
    void getTraineeByUsername_WhenAuthenticated_ShouldReturnOk() throws Exception {
        ApiResponse<TraineeDTO> response = new ApiResponse<>(true, null, traineeDTO);
        when(traineeService.getProfile("john_doe")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainees/john_doe")
                        .header("username", "john_doe")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(traineeService).checkAuthProfile("john_doe", "password123", "john_doe");
        verify(traineeService).getProfile("john_doe");
    }

    @Test
    void getTraineeByUsername_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        doThrow(new DomainException("Invalid username or password")).when(traineeService)
                .checkAuthProfile("john_doe", "wrongpassword", "john_doe");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainees/john_doe")
                        .header("username", "john_doe")
                        .header("password", "wrongpassword"))
                .andExpect(status().is4xxClientError());

        verify(traineeService).checkAuthProfile("john_doe", "wrongpassword", "john_doe");
        verify(traineeService, never()).getProfile(any());
    }

    @Test
    void updateTrainee_WhenAuthenticated_ShouldReturnAccepted() throws Exception {
        ApiResponse<TraineeDTO> response = new ApiResponse<>(true, null, traineeDTO);
        when(traineeService.updateProfile(any(), eq(1L))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/trainees/1")
                        .header("username", "john_doe")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(traineeDTO)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.data.firstName").value("John"));

        verify(traineeService).checkAuthProfile("john_doe", "password123", 1L);
        verify(traineeService).updateProfile(any(), eq(1L));
    }

    @Test
    void deleteTrainee_WhenAuthenticated_ShouldReturnNoContent() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, null, null);
        when(traineeService.deleteTraineeProfile("john_doe")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/trainees/delete")
                        .header("username", "john_doe")
                        .header("password", "password123")
                        .param("username", "john_doe"))
                .andExpect(status().isNoContent());

        verify(traineeService).checkAuthProfile("john_doe", "password123", "john_doe");
        verify(traineeService).deleteTraineeProfile("john_doe");
    }

    @Test
    void activateOrDeactivate_WhenAuthenticated_ShouldReturnOk() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, null, null);
        when(traineeService.activateOrDeactivate(any())).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.patch("/v1/trainees")
                        .header("username", "john_doe")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateDeactiveRequest)))
                .andExpect(status().isOk());

        verify(traineeService).checkAuthProfile("john_doe", "password123", "john_doe");
        verify(traineeService).activateOrDeactivate(any());
    }

    @Test
    void getNotAssignedActiveTrainers_WhenAuthenticated_ShouldReturnOk() throws Exception {
        TrainerDTO trainerDTO = new TrainerDTO();
        trainerDTO.setUsername("jane_smith");
        ApiResponse<List<TrainerDTO>> response = new ApiResponse<>(true, null, List.of(trainerDTO));
        when(traineeService.getNotAssignedActiveTrainers("john_doe")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainees/not-assigned")
                        .header("username", "john_doe")
                        .header("password", "password123")
                        .param("username", "john_doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].username").value("jane_smith"));

        verify(traineeService).checkAuthProfile("john_doe", "password123", "john_doe");
        verify(traineeService).getNotAssignedActiveTrainers("john_doe");
    }
}
