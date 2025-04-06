package com.epam.training.spring_boot_epam.dto.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCreateDTO {
    @NotNull(message = "Please enter your first name")
    private String firstName;

    @NotNull(message = "Please enter your first name")
    private String lastName;

    @NotNull(message = "Please select training type")
    private Long trainingTypeId;
}