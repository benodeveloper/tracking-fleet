package com.tracking.fleet.vehicle.domain.enums;

/**
 * Lifecycle states of a vehicle.
 */
public enum VehicleStatus {

  /** Vehicle is available for service. */
  ACTIVE,

  /** Vehicle is temporarily out of service. */
  INACTIVE,

  /** Vehicle is undergoing maintenance. */
  UNDER_MAINTENANCE,

  /** Vehicle has been permanently retired. */
  RETIRED
}
