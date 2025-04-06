package com.epam.training.spring_boot_epam.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "app_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 16)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 16)
    private String lastName;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false, length = 24)
    private String password;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "active", nullable = false)
    private Boolean active;

    public User(String firstName, String lastName, String username, String password, Boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.active = active;
    }

    public User(String firstName, String lastName, Boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
    }

    public User(Long id) {
        this.id = id;
    }
}
