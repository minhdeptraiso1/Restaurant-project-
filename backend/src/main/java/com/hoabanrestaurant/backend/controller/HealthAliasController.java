package com.hoabanrestaurant.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthAliasController {

    private final HealthEndpoint healthEndpoint;

    @Autowired
    public HealthAliasController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public HealthComponent health() {
        return healthEndpoint.health();
    }
}