package com.tracking.fleet.vehicle.application.exception;

import com.tracking.fleet.vehicle.domain.VehicleId;

/**
 * Raised when a vehicle exists but has no recorded locations.
 */
public class VehicleLocationNotFoundException extends RuntimeException {

  /**
   * Creates the exception for a vehicle.
   *
   * @param vehicleId vehicle identifier
   */
  public VehicleLocationNotFoundException(VehicleId vehicleId) {
    super("No location found for vehicle: " + vehicleId.value());
  }
}
