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
import com.epam.training.spring_boot_epam.util.DomainUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/v1/trainers")
@RequiredArgsConstructor
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final DomainUtils domainUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<AuthDTO>> createTrainer(@Valid @RequestBody TrainerCreateDTO createDTO) {
        ApiResponse<AuthDTO> response = trainerService.createProfile(createDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<TrainerDTO>> getTrainerByUsername(@PathVariable String username) {
        trainerService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), username);

        ApiResponse<TrainerDTO> response = trainerService.getProfile(username);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainings")
    public ResponseEntity<ApiResponse<List<TrainerFilterResponseDTO>>> getTrainerTrainings(@Valid @RequestBody TrainerTrainingsFilter filter){
        ApiResponse<List<TrainerFilterResponseDTO>> response = trainingService.getTrainerTrainings(filter);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("")
    public ResponseEntity<ApiResponse<TrainerDTO>> updateTrainer(@Valid @RequestBody TrainerDTO trainerDTO) {
        trainerService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), domainUtils.getCurrentUser().getId());

        ApiResponse<TrainerDTO> response = trainerService.updateProfile(trainerDTO, domainUtils.getCurrentUser().getId());
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteTrainer(@RequestParam String username) {
        trainerService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), username);

        ApiResponse<Void> response = trainerService.deleteProfile(username);
        return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Void>> activateOrDeactivate(@Valid @RequestBody ActivateDeactiveRequest request) {
        trainerService.checkAuthProfile(domainUtils.getCurrentUser().getUsername(), domainUtils.getCurrentUser().getPassword(), request.getUsername());

        ApiResponse<Void> response = trainerService.activateOrDeactivate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
