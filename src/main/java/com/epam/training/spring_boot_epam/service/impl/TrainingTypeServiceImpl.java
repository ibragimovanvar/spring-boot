package com.epam.training.spring_boot_epam.service.impl;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.mapper.TrainingTypeMapper;
import com.epam.training.spring_boot_epam.repository.TrainingTypeDao;
import com.epam.training.spring_boot_epam.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingTypeServiceImpl implements TrainingTypeService {
    private final TrainingTypeDao trainingTypeDao;
    private final TrainingTypeMapper trainingTypeMapper;

    @Override
    public ApiResponse<List<TrainingTypeDTO>> getTrainingTypes() {

        List<TrainingTypeDTO> dtoList = trainingTypeDao
                .findAll()
                .stream()
                .map(trainingTypeMapper::toDto)
                .collect(Collectors.toList());

        return new ApiResponse<>(true, null, dtoList);
    }

    @Override
    public TrainingType getTrainingType(Long id) {
        return trainingTypeDao.findById(id)
                .orElseThrow(() -> new DomainException("Training type not found"));
    }
}
