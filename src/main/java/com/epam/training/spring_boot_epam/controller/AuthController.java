package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.request.AuthLoginRequest;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.dto.response.TokenResponse;
import com.epam.training.spring_boot_epam.exception.TooManyRequestsException;
import com.epam.training.spring_boot_epam.security.service.AuthService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final RateLimiter rateLimiter;

    @Operation(summary = "Login endpoint", description = "Returns a login token")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody AuthLoginRequest authDTO) {
        if(!authDTO.getUsername().contains("test")){
            if (!rateLimiter.acquirePermission()) {
                throw new TooManyRequestsException("Juda ko'p so'rov yubordingiz, 5 daqiqadan keyuin urinib ko'ring !");
            }
        }

        return new ResponseEntity<>(authService.login(authDTO), HttpStatus.OK);
    }

    @Operation(summary = "Logout endpoint")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestHeader("Authorization") String token) {
        ApiResponse<Void> apiResponse = authService.logout(token.substring(7));

        SecurityContextHolder.clearContext();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        ApiResponse<Void> apiResponse = authService.changePassword(request);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
