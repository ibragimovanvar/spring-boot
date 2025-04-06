package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.TrainingTypeDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.service.TrainingTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/v1/training-types", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TrainingTypeDTO>>> getTrainingTypes() {
        ApiResponse<List<TrainingTypeDTO>> result = trainingTypeService.getTrainingTypes();

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
