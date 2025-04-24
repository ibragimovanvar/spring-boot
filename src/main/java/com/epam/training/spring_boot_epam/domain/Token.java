package com.epam.training.spring_boot_epam.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "jwt_tokens")
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token", length = 1000, nullable = false, updatable = false)
    private String token;

    @Column(name = "username", length = 100, nullable = false, updatable = false)
    private String username;

    @Column(name = "expired")
    private Boolean expired;
}
