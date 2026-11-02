package com.tracking.fleet.vehicle.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier of a {@link com.tracking.fleet.vehicle.domain.model.Vehicle}.
 *
 * <p>Using a dedicated type instead of a raw {@link UUID} prevents identifiers from being mixed up
 * with other UUID-based values.
 *
 * @param value underlying UUID value
 */
public record VehicleId(UUID value) {

  /**
   * Creates a vehicle identifier.
   *
   * @param value underlying UUID value, must not be {@code null}
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public VehicleId {
    Objects.requireNonNull(value, "Vehicle ID cannot be null");
  }

  /**
   * Generates a new random vehicle identifier.
   *
   * @return a new identifier backed by a random UUID
   */
  public static VehicleId generate() {
    return new VehicleId(UUID.randomUUID());
  }

  /**
   * Creates an identifier from an existing UUID.
   *
   * @param value UUID to wrap, must not be {@code null}
   * @return identifier for the supplied UUID
   */
  public static VehicleId of(UUID value) {
    return new VehicleId(value);
  }
}
