package com.epam.training.spring_boot_epam.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordChangeRequest {

    @NotNull(message = "Please enter your username")
    private String username;

    @NotNull(message = "Please enter your old password")
    private String oldPassword;

    @NotNull(message = "Please enter your new password")
    private String newPassword;
}