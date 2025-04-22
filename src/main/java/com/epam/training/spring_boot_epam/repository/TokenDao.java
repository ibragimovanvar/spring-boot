package com.epam.training.spring_boot_epam.repository;

import com.epam.training.spring_boot_epam.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenDao extends JpaRepository<Token, Long> {
    boolean existsByToken(String token);
    Optional<Token> findByToken(String token);
}
