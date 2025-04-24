package com.epam.training.spring_boot_epam.mapper;

import com.epam.training.spring_boot_epam.domain.TrainingType;
import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingType toEntity(TrainingTypeDTO trainingTypeDTO);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "trainingTypeName", target = "trainingTypeName")
    TrainingTypeDTO toDto(TrainingType trainingType);
}
