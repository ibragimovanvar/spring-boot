package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.epam.training.spring_boot_epam.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuthDTO>> createTrainer(@Valid @RequestBody TraineeCreateDTO createDTO) {
        ApiResponse<AuthDTO> response = traineeService.createProfile(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<TraineeDTO>> getTraineeByUsername(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @PathVariable String username) {
        traineeService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<TraineeDTO> response = traineeService.getProfile(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<ApiResponse<List<TraineeFilterResponseDTO>>> getTraineeTrainings(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @PathVariable String username, @Valid @RequestBody TraineeTrainingsFilter filter){
        if(username == null || username.isBlank()){
            return new ResponseEntity<>(new ApiResponse<>(false, "Missing username", null), HttpStatus.BAD_REQUEST);
        }

        filter.setUsername(username);
        ApiResponse<List<TraineeFilterResponseDTO>> response = trainingService.getTraineeTrainings(headerUsername, password, filter);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TraineeDTO>> updateTrainee(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody TraineeDTO traineeDTO, @PathVariable Long id) {
        traineeService.checkAuthProfile(headerUsername, password, id);

        ApiResponse<TraineeDTO> response = traineeService.updateProfile(traineeDTO, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteTrainee(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @RequestParam String username) {
        traineeService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<Void> response = traineeService.deleteTraineeProfile(username);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> activateOrDeactivate(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody ActivateDeactiveRequest request) {
        traineeService.checkAuthProfile(headerUsername, password, request.getUsername());

        ApiResponse<Void> response = traineeService.activateOrDeactivate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/not-assigned")
    public ResponseEntity<ApiResponse<List<TrainerDTO>>> getNotAssignedActiveTrainers(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @RequestParam String username) {
        traineeService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<List<TrainerDTO>> response = traineeService.getNotAssignedActiveTrainers(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
