package com.epam.training.spring_boot_epam.security.service.impl;

import com.epam.training.spring_boot_epam.domain.Token;
import com.epam.training.spring_boot_epam.dto.request.PasswordChangeRequest;
import com.epam.training.spring_boot_epam.dto.response.TokenResponse;
import com.epam.training.spring_boot_epam.exception.AuthorizationException;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.repository.TokenDao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.epam.training.spring_boot_epam.dto.request.AuthLoginRequest;
import com.epam.training.spring_boot_epam.dto.response.ApiResponse;
import com.epam.training.spring_boot_epam.domain.User;
import com.epam.training.spring_boot_epam.repository.UserDao;
import com.epam.training.spring_boot_epam.security.service.AuthService;
import com.epam.training.spring_boot_epam.security.service.JwtService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    @Qualifier(value = "userDao")
    private final UserDao userRepository;
    private final JwtService jwtService;
    private final TokenDao tokenDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<TokenResponse> login(AuthLoginRequest loginDto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

            User authUser = userRepository.findByUsername(loginDto.getUsername())
                    .orElseThrow(() -> new DomainException("User with username %s not found".formatted(loginDto.getUsername())));

            String jwt = jwtService.generateToken(authUser);
            long expiresIn = jwtService.getExpirationInSeconds();

            return new ApiResponse<>(true, "You logged in successfully", new TokenResponse(jwt, expiresIn));
        } catch (DomainException e) {
            return new ApiResponse<>(false, "Authentication failed: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<Void> logout(String token) {
        String extractedUsername = jwtService.extractUsername(token);

        Token userToken = tokenDao.findByTokenAndExpiredFalse(token)
                .orElseThrow(() -> new AuthorizationException("Token not found. Please login first"));

        if(!userToken.getUsername().equals(extractedUsername)) {
            throw new AuthorizationException("Token not found. Please login first");
        }

        userToken.setExpired(true);
        tokenDao.save(userToken);

        return new ApiResponse<>(true, "You logged out successfully", null);
    }

    @Override
    public ApiResponse<Void> changePassword(PasswordChangeRequest request) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        apiResponse.setData(null);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if(passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
                throw new DomainException("Failed to change password. New password should not be like old password.");
            }

            if (userRepository.updatePassword(request.getUsername(), passwordEncoder.encode(request.getNewPassword()))) {
                apiResponse.setMessage("Password changed successfully");
                apiResponse.setSuccess(true);
                return apiResponse;
            }
        }



        throw new DomainException("Failed to change password. Please verify your old password correctly.");
    }


}