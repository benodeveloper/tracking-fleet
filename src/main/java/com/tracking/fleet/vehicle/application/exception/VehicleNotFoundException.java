package com.tracking.fleet.vehicle.application.exception;

import com.tracking.fleet.vehicle.domain.VehicleId;
import java.util.UUID;

/**
 * Raised when a vehicle cannot be found.
 */
public class VehicleNotFoundException extends RuntimeException {

  /**
   * Creates the exception for a typed vehicle identifier.
   *
   * @param id missing vehicle identifier
   */
  public VehicleNotFoundException(VehicleId id) {
    super("Vehicle not found: " + id.value());
  }

  /**
   * Creates the exception for a raw vehicle identifier.
   *
   * @param id missing vehicle identifier
   */
  public VehicleNotFoundException(UUID id) {
    super("Vehicle not found: " + id);
  }
}
