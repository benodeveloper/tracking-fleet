package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for changing a vehicle lifecycle status.
 *
 * @param status requested vehicle status
 */
@Schema(name = "UpdateVehicleStatusRequest", description = "Requested vehicle lifecycle status")
public record UpdateVehicleStatusRequest(
    @Schema(description = "New vehicle status", example = "UNDER_MAINTENANCE")
        @NotNull(message = "Vehicle status is required")
        VehicleStatus status) {}
