package com.epam.training.spring_boot_epam.mapper;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {
    TrainingType toEntity(TrainingTypeDTO trainingTypeDTO);
    TrainingTypeDTO toDto(TrainingType trainingType);
}
