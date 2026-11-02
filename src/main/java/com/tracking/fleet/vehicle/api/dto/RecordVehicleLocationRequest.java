package com.tracking.fleet.vehicle.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request body for recording a vehicle location.
 *
 * @param latitude latitude in decimal degrees
 * @param longitude longitude in decimal degrees
 * @param speedKph optional speed in kilometres per hour
 * @param heading optional heading in degrees
 * @param accuracyMeters optional horizontal accuracy in metres
 * @param recordedAt time at which the device recorded the position
 */
public record RecordVehicleLocationRequest(
    @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90")
        @DecimalMax(value = "90.0", message = "Latitude must not exceed 90")
        @Digits(integer = 3, fraction = 7, message = "Latitude must have at most 7 decimals")
        BigDecimal latitude,
    @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180")
        @DecimalMax(value = "180.0", message = "Longitude must not exceed 180")
        @Digits(integer = 3, fraction = 7, message = "Longitude must have at most 7 decimals")
        BigDecimal longitude,
    @DecimalMin(value = "0.0", message = "Speed cannot be negative")
        @Digits(integer = 6, fraction = 2, message = "Speed must have at most 6 digits and 2 decimals")
        BigDecimal speedKph,
    @DecimalMin(value = "0.0", message = "Heading must be at least 0")
        @DecimalMax(value = "359.99", message = "Heading must be less than 360")
        @Digits(integer = 3, fraction = 2, message = "Heading must have at most 2 decimals")
        BigDecimal heading,
    @DecimalMin(value = "0.0", message = "Accuracy cannot be negative")
        @Digits(
            integer = 6,
            fraction = 2,
            message = "Accuracy must have at most 6 digits and 2 decimals")
        BigDecimal accuracyMeters,
    @NotNull(message = "Recorded time is required") Instant recordedAt) {}
