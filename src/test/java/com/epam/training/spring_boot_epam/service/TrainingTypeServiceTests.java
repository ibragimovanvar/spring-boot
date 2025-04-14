package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.mapper.TrainingTypeMapper;
import com.epam.training.spring_boot_epam.repository.TrainingTypeDao;
import com.epam.training.spring_boot_epam.service.impl.TrainingTypeServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;


@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTests {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private TrainingTypeMapper trainingTypeMapper;

    @InjectMocks
    private TrainingTypeServiceImpl trainingTypeService;

    private TrainingType trainingType;
    private TrainingTypeDTO trainingTypeDTO;

    @BeforeEach
    void setUp() {
        trainingType = new TrainingType(1L, "Yoga");
        trainingTypeDTO = new TrainingTypeDTO();
        trainingTypeDTO.setId(1L);
        trainingTypeDTO.setTrainingTypeName("Yoga");
    }

    @Test
    void getTrainingTypes_WhenTypesExist_ShouldReturnTrainingTypeDTOs() {
        when(trainingTypeDao.findAll()).thenReturn(List.of(trainingType));
        when(trainingTypeMapper.toDto(trainingType)).thenReturn(trainingTypeDTO);

        ApiResponse<List<TrainingTypeDTO>> response = trainingTypeService.getTrainingTypes();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().get(0).getId()).isEqualTo(1L);
        assertThat(response.getData().get(0).getTrainingTypeName()).isEqualTo("Yoga");
        verify(trainingTypeDao, times(1)).findAll();
        verify(trainingTypeMapper, times(1)).toDto(trainingType);
    }

    @Test
    void getTrainingTypes_WhenNoTypesExist_ShouldReturnEmptyList() {
        when(trainingTypeDao.findAll()).thenReturn(Collections.emptyList());

        ApiResponse<List<TrainingTypeDTO>> response = trainingTypeService.getTrainingTypes();

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getData()).isEmpty();
        verify(trainingTypeDao, times(1)).findAll();
        verify(trainingTypeMapper, never()).toDto(any());
    }

    @Test
    void getTrainingType_WhenTypeExists_ShouldReturnTrainingType() {
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(trainingType));

        TrainingType result = trainingTypeService.getTrainingType(1L);

        assertThat(result).isEqualTo(trainingType);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTrainingTypeName()).isEqualTo("Yoga");
        verify(trainingTypeDao, times(1)).findById(1L);
    }

    @Test
    void getTrainingType_WhenTypeDoesNotExist_ShouldThrowDomainException() {
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> trainingTypeService.getTrainingType(1L));
        assertThat(exception.getMessage()).isEqualTo("Training type not found");
        verify(trainingTypeDao, times(1)).findById(1L);
    }
}