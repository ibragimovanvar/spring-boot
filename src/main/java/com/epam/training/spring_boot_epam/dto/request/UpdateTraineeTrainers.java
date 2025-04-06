package com.epam.training.spring_boot_epam.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTraineeTrainers {

    @NotNull(message = "Please enter username")
    private String traineeUsername;

    @NotNull(message = "Trainers are required")
    private List<String> trainersList;
}
