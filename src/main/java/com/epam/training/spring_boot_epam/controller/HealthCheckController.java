package com.epam.training.spring_boot_epam.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @Autowired
    private HealthEndpoint healthEndpoint;

    @GetMapping("/check-health")
    public HealthComponent getHealth() {
        return  healthEndpoint.health();
    }
}