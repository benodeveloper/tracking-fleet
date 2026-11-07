package com.tracking.fleet.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record UserAccount(
    UUID id,
    String username,
    String passwordHash,
    String displayName,
    String role,
    UUID assignedVehicleId,
    Instant createdAt,
    Instant updatedAt) {}
