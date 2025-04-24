package com.epam.training.spring_boot_epam.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TraineeTrainersUpdate {
    @NotEmpty
    private List<String> trainers;
}