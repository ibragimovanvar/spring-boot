package com.epam.training.spring_boot_epam.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TraineeFilterResponseDTO {
    private Long id;

    private String trainerFirstname;

    private String trainingName;

    private String trainingType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime trainingDateTime;

    private Integer trainingDurationInHours;
}
