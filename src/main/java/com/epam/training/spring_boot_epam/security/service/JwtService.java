package com.epam.training.spring_boot_epam.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public interface JwtService {
    String generateToken(UserDetails userDetails);
    String extractUsername(String token);
    long getExpirationInSeconds();
    boolean isTokenValid(String token, UserDetails userDetails);
}