package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeTrainersUpdate;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.service.TraineeService;
import com.epam.training.spring_boot_epam.service.TrainingService;
import com.epam.training.spring_boot_epam.util.DomainUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/v1/trainees")
@RequiredArgsConstructor
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainingService trainingService;
    private final DomainUtils domainUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<AuthDTO>> createTrainer(@Valid @RequestBody TraineeCreateDTO createDTO) {
        ApiResponse<AuthDTO> response = traineeService.createProfile(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<TraineeDTO>> getTraineeByUsername(@PathVariable String username) {
        ApiResponse<TraineeDTO> response = traineeService.getProfile(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<ApiResponse<List<TraineeFilterResponseDTO>>> getTraineeTrainings(@PathVariable String username, @Valid @RequestBody TraineeTrainingsFilter filter){
        if(username == null || username.isBlank()){
            return new ResponseEntity<>(new ApiResponse<>(false, "Missing username", null), HttpStatus.BAD_REQUEST);
        }

        filter.setUsername(username);
        ApiResponse<List<TraineeFilterResponseDTO>> response = trainingService.getTraineeTrainings(filter);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<ApiResponse<TraineeDTO>> updateTrainee(@Valid @RequestBody TraineeDTO traineeDTO) {
        traineeService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), domainUtils.getCurrentUser().getId());

        ApiResponse<TraineeDTO> response = traineeService.updateProfile(traineeDTO, domainUtils.getCurrentUser().getId());
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PutMapping("/{username}/trainers")
    public ResponseEntity<ApiResponse<Void>> updateTraineeTrainers(@Valid @RequestBody TraineeTrainersUpdate trainersUpdate, @PathVariable String username) {
        traineeService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), username);

        ApiResponse<Void> response = traineeService.updateTraineeTrainers(trainersUpdate, username);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteTrainee(@RequestParam String username) {
        traineeService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), username);

        ApiResponse<Void> response = traineeService.deleteTraineeProfile(username);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> activateOrDeactivate(@Valid @RequestBody ActivateDeactiveRequest request) {
        traineeService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), request.getUsername());

        ApiResponse<Void> response = traineeService.activateOrDeactivate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/not-assigned")
    public ResponseEntity<ApiResponse<List<TrainerDTO>>> getNotAssignedActiveTrainers(@RequestParam String username) {
        traineeService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), username);

        ApiResponse<List<TrainerDTO>> response = traineeService.getNotAssignedActiveTrainers(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
