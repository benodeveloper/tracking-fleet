package com.tracking.fleet.vehicle.domain.model;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import java.util.Objects;

/**
 * Immutable descriptive specification of a vehicle.
 *
 * <p>Brand and model values are trimmed and validated when the specification is created.
 *
 * @param brand vehicle manufacturer, must not be blank
 * @param model vehicle model, must not be blank
 * @param manufacturingYear year of manufacture, between 1886 and 2100
 * @param type vehicle category
 * @param fuelType fuel type used by the vehicle
 * @param transmissionType transmission type of the vehicle
 */
public record VehicleSpecification(
    String brand,
    String model,
    int manufacturingYear,
    VehicleType type,
    FuelType fuelType,
    TransmissionType transmissionType) {

  /**
   * Validates and normalizes the vehicle specification.
   *
   * @throws NullPointerException if any enum or text component is {@code null}
   * @throws IllegalArgumentException if brand or model is blank or the manufacturing year is
   *     outside the supported range
   */
  public VehicleSpecification {
    Objects.requireNonNull(brand, "Brand cannot be null");
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(type, "Vehicle type cannot be null");
    Objects.requireNonNull(fuelType, "Fuel type cannot be null");
    Objects.requireNonNull(transmissionType, "Transmission type cannot be null");

    brand = brand.trim();
    model = model.trim();

    if (brand.isBlank()) {
      throw new IllegalArgumentException("Brand cannot be blank");
    }

    if (model.isBlank()) {
      throw new IllegalArgumentException("Model cannot be blank");
    }

    if (manufacturingYear < 1886 || manufacturingYear > 2100) {
      throw new IllegalArgumentException("Invalid manufacturing year");
    }
  }
}
