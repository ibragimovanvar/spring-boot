package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
class TrainingTypeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TrainingTypeService trainingTypeService;

    private TrainingTypeDTO trainingTypeDTO;

    @BeforeEach
    void setUp() {
        trainingTypeDTO = new TrainingTypeDTO();
        trainingTypeDTO.setId(1L);
        trainingTypeDTO.setTrainingTypeName("Yoga");
    }

    @Test
    void getTrainingTypes_WhenTypesExist_ShouldReturnOk() throws Exception {
        ApiResponse<List<TrainingTypeDTO>> response = new ApiResponse<>(true, null, Collections.singletonList(trainingTypeDTO));
        when(trainingTypeService.getTrainingTypes()).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/training-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].trainingTypeName").value("Yoga"));

        verify(trainingTypeService, times(1)).getTrainingTypes();
    }

    @Test
    void getTrainingTypes_WhenNoTypesExist_ShouldReturnOkWithEmptyList() throws Exception {
        ApiResponse<List<TrainingTypeDTO>> response = new ApiResponse<>(true, null, Collections.emptyList());
        when(trainingTypeService.getTrainingTypes()).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/training-types")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.data").isEmpty());

        verify(trainingTypeService, times(1)).getTrainingTypes();
    }
}