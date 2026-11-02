package com.tracking.fleet.vehicle.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request body for updating a vehicle mileage.
 *
 * @param mileage new mileage
 */
@Schema(name = "UpdateVehicleMileageRequest", description = "New odometer reading for a vehicle")
public record UpdateVehicleMileageRequest(
    @Schema(description = "New mileage in kilometres; mileage can only increase", example = "152430.5")
        @NotNull(message = "Mileage is required")
        @DecimalMin(value = "0.0", message = "Mileage cannot be negative")
        @Digits(
            integer = 10,
            fraction = 2,
            message = "Mileage must have at most 10 digits and 2 decimals")
        BigDecimal mileage) {}
