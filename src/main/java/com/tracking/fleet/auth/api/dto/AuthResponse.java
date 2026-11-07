package com.tracking.fleet.auth.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
    String token,
    String tokenType,
    long expiresInSeconds,
    String username,
    String displayName,
    String role) {}
