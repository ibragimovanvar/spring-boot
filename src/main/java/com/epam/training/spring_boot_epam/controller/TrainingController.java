package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/trainings")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @GetMapping("/trainee-trainings")
    public ResponseEntity<ApiResponse<List<TraineeFilterResponseDTO>>> getTraineeTrainings(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody TraineeTrainingsFilter filter){
        ApiResponse<List<TraineeFilterResponseDTO>> response = trainingService.getTraineeTrainings(headerUsername, password, filter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/trainer-trainings")
    public ResponseEntity<ApiResponse<List<TrainerFilterResponseDTO>>> getTrainerTrainings(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody TrainerTrainingsFilter filter){
        ApiResponse<List<TrainerFilterResponseDTO>> response = trainingService.getTrainerTrainings(headerUsername, password, filter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createTraining(@Valid @RequestBody TrainingDTO dto){
        ApiResponse<Void> response = trainingService.addTraining(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
