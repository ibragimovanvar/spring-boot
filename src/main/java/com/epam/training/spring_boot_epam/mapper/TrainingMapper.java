package com.epam.training.spring_boot_epam.mapper;

import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {


    @Mapping(source = "id", target = "id")
    @Mapping(source = "trainingName", target = "trainingName")
    @Mapping(source = "trainingDateTime", target = "trainingDateTime")
    @Mapping(source = "trainingDurationInMinutes", target = "trainingDurationInMinutes")
    Training toEntity(TrainingDTO dto);

    TrainingDTO toDto(Training entity);
}
