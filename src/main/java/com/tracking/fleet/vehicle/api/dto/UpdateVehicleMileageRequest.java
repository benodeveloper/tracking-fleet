package com.tracking.fleet.vehicle.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Request body for updating a vehicle mileage.
 *
 * @param mileage new mileage
 */
public record UpdateVehicleMileageRequest(
    @NotNull(message = "Mileage is required")
        @DecimalMin(value = "0.0", message = "Mileage cannot be negative")
        @Digits(integer = 10, fraction = 2, message = "Mileage must have at most 10 digits and 2 decimals")
        BigDecimal mileage) {}
