package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenDao extends JpaRepository<Token, Long> {
    Optional<Token> findByTokenAndExpiredFalse(String token);
    Optional<Token> findByUsernameAndTokenAndExpiredFalse(String username, String token);
    List<Token> findByUsername(String username);
}
