package com.epam.training.spring_boot_epam.controller;

import com.epam.training.spring_boot_epam.dto.request.AuthDTO;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.repository.UserDao;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserDao userDao;

    @Operation(summary = "Login endpoint", description = "Returns a login token")
    @GetMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(@Valid @RequestBody AuthDTO authDTO) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        if (userDao.existsByUsernameAndPassword(authDTO.getUsername(), authDTO.getPassword())) {
            apiResponse.setMessage("Login Successful");
            apiResponse.setSuccess(true);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }

        apiResponse.setMessage("Login or password wrong!");
        apiResponse.setSuccess(false);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        if (userDao.updatePassword(request.getUsername(), request.getOldPassword(), request.getNewPassword())) {
            apiResponse.setMessage("Password changed successfully");
            apiResponse.setSuccess(true);
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        }

        apiResponse.setMessage("Failed to change password. Please verify your username and old password.");
        apiResponse.setSuccess(false);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
}
