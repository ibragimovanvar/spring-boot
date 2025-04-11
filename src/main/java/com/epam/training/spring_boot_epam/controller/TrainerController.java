package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import com.epam.training.spring_boot_epam.service.TrainerService;
import com.epam.training.spring_boot_epam.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;


    @PostMapping
    public ResponseEntity<ApiResponse<AuthDTO>> createTrainer(@Valid @RequestBody TrainerCreateDTO createDTO) {
        ApiResponse<AuthDTO> response = trainerService.createProfile(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<TrainerDTO>> getTrainerByUsername(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @PathVariable String username) {
        trainerService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<TrainerDTO> response = trainerService.getProfile(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{username}/trainings")
    public ResponseEntity<ApiResponse<List<TrainerFilterResponseDTO>>> getTrainerTrainings(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @PathVariable String username, @Valid @RequestBody TrainerTrainingsFilter filter){
        if(username == null || username.isBlank()){
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        filter.setUsername(username);
        ApiResponse<List<TrainerFilterResponseDTO>> response = trainingService.getTrainerTrainings(headerUsername, password, filter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TrainerDTO>> updateTrainer(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody TrainerDTO trainerDTO, @PathVariable Long id) {
        trainerService.checkAuthProfile(headerUsername, password, id);

        ApiResponse<TrainerDTO> response = trainerService.updateProfile(trainerDTO, id);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteTrainer(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @RequestParam String username) {
        trainerService.checkAuthProfile(headerUsername, password, username);

        ApiResponse<Void> response = trainerService.deleteProfile(username);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> activateOrDeactivate(@RequestHeader(value = "username") String headerUsername, @RequestHeader(value = "password") String password, @Valid @RequestBody ActivateDeactiveRequest request) {
        trainerService.checkAuthProfile(headerUsername, password, request.getUsername());

        ApiResponse<Void> response = trainerService.activateOrDeactivate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
