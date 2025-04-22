package com.epam.training.spring_boot_epam.security.service.impl;

import com.epam.training.spring_boot_epam.domain.Token;
import com.epam.training.spring_boot_epam.dto.response.TokenResponse;
import com.epam.training.spring_boot_epam.exception.DomainException;
import com.epam.training.spring_boot_epam.repository.TokenDao;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

        Token userToken = tokenDao.findByToken(token)
                .orElseThrow(() -> new DomainException("Token not found"));

        userToken.setExpired(true);
        tokenDao.save(userToken);

        return new ApiResponse<>(true, "You logged out successfully", null);
    }


}