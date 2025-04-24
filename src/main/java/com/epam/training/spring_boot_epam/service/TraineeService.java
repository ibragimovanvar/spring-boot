package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.Trainee;
import com.epam.training.spring_boot_epam.dto.TraineeDTO;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeCreateDTO;
import com.epam.training.spring_boot_epam.dto.request.TraineeTrainersUpdate;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("traineeService")
public interface TraineeService {
    ApiResponse<TraineeDTO> getProfile(String username);
    void checkAuthProfile(String headerUsername, String password, String username);
    void checkAuthProfile(String headerUsername, String password, Long id);
    Trainee getByUsername(String username);
    ApiResponse<TraineeDTO> updateProfile(TraineeDTO trainee, Long id);
    ApiResponse<AuthDTO> createProfile(TraineeCreateDTO dto);
    ApiResponse<Void> deleteTraineeProfile(String username);
    ApiResponse<Void> activateOrDeactivate(ActivateDeactiveRequest request);
    ApiResponse<List<TrainerDTO>> getNotAssignedActiveTrainers(String username);
    ApiResponse<Void> updateTraineeTrainers(TraineeTrainersUpdate trainersUpdate, String username);
}
