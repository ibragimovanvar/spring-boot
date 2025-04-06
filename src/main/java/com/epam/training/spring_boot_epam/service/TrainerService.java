package com.epam.training.spring_boot_epam.service;

import com.epam.training.spring_boot_epam.domain.Trainer;
import com.epam.training.spring_boot_epam.dto.TrainerDTO;
import com.epam.training.spring_boot_epam.dto.request.ActivateDeactiveRequest;
import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.TrainerCreateDTO;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import org.springframework.stereotype.Service;

@Service("trainerService")
public interface TrainerService {
    void checkAuthProfile(String headerUsername, String password, String username);
    void checkAuthProfile(String headerUsername, String password, Long id);
    ApiResponse<TrainerDTO> getProfile(String username);
    Trainer getByUsername(String username);
    ApiResponse<TrainerDTO> updateProfile(TrainerDTO dto, Long id);
    ApiResponse<AuthDTO> createProfile(TrainerCreateDTO dto);
    ApiResponse<Void> deleteProfile(String username);
    ApiResponse<Void> activateOrDeactivate(ActivateDeactiveRequest request);
}
