package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API representation of a vehicle.
 *
 * @param id vehicle identifier
 * @param registrationNumber registration number
 * @param vin optional vehicle identification number
 * @param brand vehicle manufacturer
 * @param model vehicle model
 * @param manufacturingYear year of manufacture
 * @param type vehicle category
 * @param fuelType fuel type
 * @param transmissionType transmission type
 * @param status current lifecycle status
 * @param color optional color
 * @param mileage current mileage
 * @param createdAt creation timestamp
 * @param updatedAt last update timestamp
 */
@Schema(name = "VehicleResponse", description = "A fleet vehicle")
public record VehicleResponse(
    @Schema(
            description = "Vehicle identifier",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            accessMode = Schema.AccessMode.READ_ONLY)
        UUID id,
    @Schema(description = "Unique registration number", example = "AB-123-CD")
        String registrationNumber,
    @Schema(description = "Vehicle identification number", example = "1HGCM82633A004352")
        String vin,
    @Schema(description = "Vehicle manufacturer", example = "Toyota") String brand,
    @Schema(description = "Vehicle model", example = "Corolla") String model,
    @Schema(description = "Year of manufacture", example = "2020") int manufacturingYear,
    @Schema(description = "Vehicle category", example = "CAR") VehicleType type,
    @Schema(description = "Fuel type", example = "PETROL") FuelType fuelType,
    @Schema(description = "Transmission type", example = "AUTOMATIC")
        TransmissionType transmissionType,
    @Schema(description = "Current lifecycle status", example = "ACTIVE") VehicleStatus status,
    @Schema(description = "Vehicle color", example = "White") String color,
    @Schema(description = "Current mileage in kilometres", example = "152430.5")
        BigDecimal mileage,
    @Schema(
            description = "Time at which the vehicle was created",
            example = "2026-01-01T10:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY)
        Instant createdAt,
    @Schema(
            description = "Time at which the vehicle was last updated",
            example = "2026-01-02T12:30:00Z",
            accessMode = Schema.AccessMode.READ_ONLY)
        Instant updatedAt) {

  /**
   * Creates a response from a domain vehicle.
   *
   * @param vehicle domain vehicle
   * @return API representation of the vehicle
   */
  public static VehicleResponse from(Vehicle vehicle) {
    VehicleSpecification specification = vehicle.specification();

    return new VehicleResponse(
        vehicle.id().value(),
        vehicle.registrationNumber().value(),
        vehicle.vin(),
        specification.brand(),
        specification.model(),
        specification.manufacturingYear(),
        specification.type(),
        specification.fuelType(),
        specification.transmissionType(),
        vehicle.status(),
        vehicle.color(),
        vehicle.mileage(),
        vehicle.createdAt(),
        vehicle.updatedAt());
  }
}
