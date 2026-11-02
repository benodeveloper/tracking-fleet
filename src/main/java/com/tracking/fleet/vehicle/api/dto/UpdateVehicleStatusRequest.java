package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for changing a vehicle lifecycle status.
 *
 * @param status requested vehicle status
 */
public record UpdateVehicleStatusRequest(
    @NotNull(message = "Vehicle status is required") VehicleStatus status) {}
