package com.epam.training.spring_boot_epam.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
public class TrainingDTO {
    private Long id;

    @NotNull(message = "Please enter trainer username")
    private String trainerUsername;

    @NotNull(message = "Please enter trainee username")
    private String traineeUsername;

    @NotNull(message = "Please enter training name")
    private String trainingName;

    @NotNull(message = "Please enter training date time")
    @FutureOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime trainingDateTime;

    @NotNull(message = "Please enter training duration(minutes)")
    private Integer trainingDurationInMinutes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrainerUsername() {
        return trainerUsername;
    }

    public void setTrainerUsername(String trainerUsername) {
        this.trainerUsername = trainerUsername;
    }

    public String getTraineeUsername() {
        return traineeUsername;
    }

    public void setTraineeUsername(String traineeUsername) {
        this.traineeUsername = traineeUsername;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public LocalDateTime getTrainingDateTime() {
        return trainingDateTime;
    }

    public void setTrainingDateTime(LocalDateTime trainingDateTime) {
        this.trainingDateTime = trainingDateTime;
    }

    public Integer getTrainingDurationInMinutes() {
        return trainingDurationInMinutes;
    }

    public void setTrainingDurationInMinutes(Integer trainingDurationInMinutes) {
        this.trainingDurationInMinutes = trainingDurationInMinutes;
    }
}
