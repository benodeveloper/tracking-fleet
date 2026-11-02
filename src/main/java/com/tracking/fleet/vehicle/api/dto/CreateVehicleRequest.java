package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a vehicle.
 *
 * @param registrationNumber unique registration number
 * @param vin optional vehicle identification number
 * @param brand vehicle manufacturer
 * @param model vehicle model
 * @param manufacturingYear year of manufacture
 * @param type vehicle category
 * @param fuelType fuel type
 * @param transmissionType transmission type
 * @param color optional color
 */
@Schema(name = "CreateVehicleRequest", description = "Data required to register a vehicle")
public record CreateVehicleRequest(
    @Schema(description = "Unique vehicle registration number", example = "AB-123-CD")
        @NotBlank(message = "Registration number is required")
        @Size(max = 30, message = "Registration number must not exceed 30 characters")
        @Pattern(
            regexp = "^[A-Za-z0-9-]{2,30}$",
            message = "Registration number may only contain letters, digits and dashes")
        String registrationNumber,
    @Schema(
            description = "Optional vehicle identification number (VIN)",
            example = "1HGCM82633A004352")
        @Size(max = 17, message = "VIN must not exceed 17 characters")
        String vin,
    @Schema(description = "Vehicle manufacturer", example = "Toyota")
        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,
    @Schema(description = "Vehicle model", example = "Corolla")
        @NotBlank(message = "Model is required")
        @Size(max = 100, message = "Model must not exceed 100 characters")
        String model,
    @Schema(description = "Year of manufacture", example = "2020")
        @Min(value = 1886, message = "Manufacturing year must be at least 1886")
        @Max(value = 2100, message = "Manufacturing year must not exceed 2100")
        int manufacturingYear,
    @Schema(description = "Vehicle category", example = "CAR")
        @NotNull(message = "Vehicle type is required")
        VehicleType type,
    @Schema(description = "Fuel type", example = "PETROL")
        @NotNull(message = "Fuel type is required")
        FuelType fuelType,
    @Schema(description = "Transmission type", example = "AUTOMATIC")
        @NotNull(message = "Transmission type is required")
        TransmissionType transmissionType,
    @Schema(description = "Optional vehicle color", example = "White")
        @Size(max = 50, message = "Color must not exceed 50 characters")
        String color) {}
