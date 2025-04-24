package com.epam.training.spring_boot_epam.security.service;

import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.dto.response.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import com.epam.training.spring_boot_epam.dto.request.AuthLoginRequest;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;

@Service
public interface AuthService {
    ApiResponse<TokenResponse> login(AuthLoginRequest loginDto);
    ApiResponse<Void> logout(String token);
    ApiResponse<Void> changePassword(PasswordChangeRequest request);
}