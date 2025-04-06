package com.epam.training.spring_boot_epam.mapper;

import com.epam.training.spring_boot_epam.domain.Training;
import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    Training toEntity(TrainingDTO dto);
    TrainingDTO toDto(Training entity);
}
