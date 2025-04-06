package com.epam.training.spring_boot_epam.dto;


import com.epam.training.spring_boot_epam.dto.response.GetTraineeTrainerDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TraineeDTO {
    private Long id;

    @NotNull(message = "Please enter your first name")
    private String firstName;

    @NotNull(message = "Please enter your first name")
    private String lastName;

    private String address;

    @NotNull(message = "Please select active/inactive")
    private Boolean active;

    private List<GetTraineeTrainerDTO> trainerList;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
}
