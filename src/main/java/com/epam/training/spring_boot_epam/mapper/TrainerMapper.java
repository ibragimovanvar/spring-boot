package com.epam.training.spring_boot_epam.mapper;

import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.response.GetTraineeTrainerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "user.active", target = "active")
    @Mapping(source = "specialization.id", target = "trainingTypeId")
    TrainerDTO toDto(Trainer trainee);

    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    @Mapping(source = "user.username", target = "username")
    @Mapping(source = "specialization.id", target = "trainingTypeId")
    GetTraineeTrainerDTO toGetTraineeTrainerDto(Trainer trainee);

}
