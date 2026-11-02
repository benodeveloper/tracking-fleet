package com.tracking.fleet.vehicle.application.exception;

/**
 * Raised when a vehicle cannot be saved because it conflicts with an existing vehicle, usually due
 * to a duplicate registration number or VIN.
 */
public class VehicleAlreadyExistsException extends RuntimeException {

  /**
   * Creates the exception with a custom message.
   *
   * @param message explanation of the conflict
   */
  public VehicleAlreadyExistsException(String message) {
    super(message);
  }

  /**
   * Creates the exception for a duplicated field.
   *
   * @param field conflicting field name
   * @param value conflicting value
   */
  public VehicleAlreadyExistsException(String field, String value) {
    super("A vehicle with the same " + field + " already exists: " + value);
  }
}
