package com.tracking.fleet.vehicle.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly typed identifier of a recorded vehicle location.
 *
 * @param value underlying UUID value
 */
public record VehicleLocationId(UUID value) {

  /**
   * Creates a location identifier.
   *
   * @param value underlying UUID value, must not be {@code null}
   * @throws NullPointerException if {@code value} is {@code null}
   */
  public VehicleLocationId {
    Objects.requireNonNull(value, "Vehicle location ID cannot be null");
  }

  /**
   * Generates a new random location identifier.
   *
   * @return a new identifier backed by a random UUID
   */
  public static VehicleLocationId generate() {
    return new VehicleLocationId(UUID.randomUUID());
  }

  /**
   * Creates an identifier from an existing UUID.
   *
   * @param value UUID to wrap, must not be {@code null}
   * @return identifier for the supplied UUID
   */
  public static VehicleLocationId of(UUID value) {
    return new VehicleLocationId(value);
  }
}
