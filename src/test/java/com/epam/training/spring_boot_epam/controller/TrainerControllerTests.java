package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.controller.TrainerController;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.service.TrainerService;
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

@WebMvcTest(TrainerController.class)
class TrainerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainerService trainerService;

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

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainers/by-username")
                        .header("username", "jane_smith")
                        .header("password", "password123")
                        .param("username", "jane_smith"))
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

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/trainers/by-username")
                        .header("username", "jane_smith")
                        .header("password", "wrongpassword")
                        .param("username", "jane_smith"))
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

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/trainers/1")
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