package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.service.TraineeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final TraineeService traineeService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuthDTO>> createTrainer(@Valid @RequestBody TraineeCreateDTO createDTO) {
        ApiResponse<AuthDTO> response = traineeService.createProfile(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/by-username")
    public ResponseEntity<ApiResponse<TraineeDTO>> getTraineeByUsername(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @RequestParam String username) {
        traineeService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<TraineeDTO> response = traineeService.getProfile(username);
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
