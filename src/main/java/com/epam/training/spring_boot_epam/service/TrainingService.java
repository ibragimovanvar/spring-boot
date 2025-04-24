package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.dto.TrainingDTO;
import com.epam.training.spring_boot_epam.dto.filters.TraineeTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.filters.TrainerTrainingsFilter;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TraineeFilterResponseDTO;
import com.epam.training.spring_boot_epam.dto.response.TrainerFilterResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("trainingService")
public interface TrainingService {
    ApiResponse<List<TraineeFilterResponseDTO>> getTraineeTrainings(TraineeTrainingsFilter filter);

    ApiResponse<List<TrainerFilterResponseDTO>> getTrainerTrainings(TrainerTrainingsFilter filter);

    ApiResponse<Void> addTraining(TrainingDTO dto);
}
