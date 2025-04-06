package com.epam.training.spring_boot_epam.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivateDeactiveRequest {

    @NotNull(message = "Please enter your username")
    private String username;

    @NotNull(message = "Please select active/inactive")
    private Boolean active;

}
