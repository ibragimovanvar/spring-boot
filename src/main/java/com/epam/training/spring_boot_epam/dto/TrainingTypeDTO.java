package com.epam.training.spring_boot_epam.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrainingTypeDTO {
    private Long id;
    private String trainingTypeName;
}
