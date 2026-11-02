package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
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
public record VehicleResponse(
    UUID id,
    String registrationNumber,
    String vin,
    String brand,
    String model,
    int manufacturingYear,
    VehicleType type,
    FuelType fuelType,
    TransmissionType transmissionType,
    VehicleStatus status,
    String color,
    BigDecimal mileage,
    Instant createdAt,
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
