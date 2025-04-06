package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("trainingTypeService")
public interface TrainingTypeService {
    ApiResponse<List<TrainingTypeDTO>> getTrainingTypes();
    TrainingType getTrainingType(Long id);
}
