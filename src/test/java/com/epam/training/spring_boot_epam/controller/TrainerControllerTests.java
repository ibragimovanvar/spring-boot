package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.service.TrainerService;
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

import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainerController.class)
class TrainerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerService trainerService;

    @MockBean
    private TrainingService trainingService;

    @Autowired
    private ObjectMapper objectMapper;

    private TrainerCreateDTO trainerCreateDTO;
    private TrainerDTO trainerDTO;
    private AuthDTO authDTO;
    private ActivateDeactiveRequest activateDeactiveRequest;

    @BeforeEach
    void setUp() {
        trainerCreateDTO = new TrainerCreateDTO("Jane", "Smith", 1L);
        trainerDTO = new TrainerDTO();
        trainerDTO.setUsername("jane_smith");
        trainerDTO.setFirstName("Jane");
        trainerDTO.setLastName("Smith");
        trainerDTO.setActive(true);
        trainerDTO.setTrainingTypeId(1L);
        authDTO = new AuthDTO("jane_smith", "password123");
        activateDeactiveRequest = new ActivateDeactiveRequest("jane_smith", true);
    }

    @Test
    void getTrainerTrainings_WhenValid_ShouldReturnOk() throws Exception {
        TrainerTrainingsFilter filter = new TrainerTrainingsFilter();

        TrainerFilterResponseDTO responseDTO = new TrainerFilterResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTrainingName("Morning Yoga");
        responseDTO.setTraineeFirstname("John");
        responseDTO.setTrainingType("Yoga");

        ApiResponse<List<TrainerFilterResponseDTO>> response =
                new ApiResponse<>(true, null, List.of(responseDTO));

        when(trainingService.getTrainerTrainings(eq("jane_smith"), eq("password123"), any(TrainerTrainingsFilter.class)))
                .thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainers/jane_smith/trainings")
                        .header("username", "jane_smith")
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
        when(trainerService.createProfile(any(TrainerCreateDTO.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.username").value("jane_smith"));

        verify(trainerService, times(1)).createProfile(any(TrainerCreateDTO.class));
    }

    @Test
    void getTrainerByUsername_WhenAuthenticated_ShouldReturnOk() throws Exception {
        ApiResponse<TrainerDTO> response = new ApiResponse<>(true, null, trainerDTO);
        when(trainerService.getProfile("jane_smith")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainers/jane_smith")
                        .header("username", "jane_smith")
                        .header("password", "password123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.username").value("jane_smith"));

        verify(trainerService, times(1)).checkAuthProfile("jane_smith", "password123", "jane_smith");
        verify(trainerService, times(1)).getProfile("jane_smith");
    }

    @Test
    void getTrainerByUsername_WhenNotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        doThrow(new DomainException("Invalid username or password")).when(trainerService)
                .checkAuthProfile("jane_smith", "wrongpassword", "jane_smith");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainers/jane_smith")
                        .header("username", "jane_smith")
                        .header("password", "wrongpassword"))
                .andExpect(status().is4xxClientError());

        verify(trainerService, times(1)).checkAuthProfile("jane_smith", "wrongpassword", "jane_smith");
        verify(trainerService, never()).getProfile(anyString());
    }

    @Test
    void updateTrainer_WhenAuthenticated_ShouldReturnAccepted() throws Exception {
        ApiResponse<TrainerDTO> response = new ApiResponse<>(true, null, trainerDTO);
        when(trainerService.updateProfile(any(TrainerDTO.class), eq(1L))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/trainers/1")
                        .header("username", "jane_smith")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(trainerDTO)))
                .andExpect(status().isAccepted())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data.username").value("jane_smith"));

        verify(trainerService, times(1)).checkAuthProfile("jane_smith", "password123", 1L);
        verify(trainerService, times(1)).updateProfile(any(TrainerDTO.class), eq(1L));
    }

    @Test
    void deleteTrainer_WhenAuthenticated_ShouldReturnNoContent() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, null, null);
        when(trainerService.deleteProfile("jane_smith")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/trainers")
                        .header("username", "jane_smith")
                        .header("password", "password123")
                        .param("username", "jane_smith"))
                .andExpect(status().isNoContent())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(trainerService, times(1)).checkAuthProfile("jane_smith", "password123", "jane_smith");
        verify(trainerService, times(1)).deleteProfile("jane_smith");
    }

    @Test
    void activateOrDeactivate_WhenAuthenticated_ShouldReturnOk() throws Exception {
        ApiResponse<Void> response = new ApiResponse<>(true, null, null);
        when(trainerService.activateOrDeactivate(any(ActivateDeactiveRequest.class))).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.patch("/v1/trainers")
                        .header("username", "jane_smith")
                        .header("password", "password123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(activateDeactiveRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(trainerService, times(1)).checkAuthProfile("jane_smith", "password123", "jane_smith");
        verify(trainerService, times(1)).activateOrDeactivate(any(ActivateDeactiveRequest.class));
    }
}