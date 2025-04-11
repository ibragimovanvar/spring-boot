package com.epam.training.spring_boot_epam.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class HealthCheckController {


    private final HealthEndpoint healthEndpoint;

    @GetMapping("/check-health")
    public HealthComponent getHealth() {
        return  healthEndpoint.health();
    }
}