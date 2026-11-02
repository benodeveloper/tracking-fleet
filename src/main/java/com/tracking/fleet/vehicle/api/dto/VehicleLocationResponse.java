package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * API representation of a recorded vehicle location.
 *
 * @param id location identifier
 * @param vehicleId vehicle identifier
 * @param latitude latitude in decimal degrees
 * @param longitude longitude in decimal degrees
 * @param speedKph optional speed in kilometres per hour
 * @param heading optional heading in degrees
 * @param accuracyMeters optional horizontal accuracy in metres
 * @param recordedAt time at which the device recorded the position
 * @param receivedAt time at which the position entered the system
 */
public record VehicleLocationResponse(
    UUID id,
    UUID vehicleId,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal speedKph,
    BigDecimal heading,
    BigDecimal accuracyMeters,
    Instant recordedAt,
    Instant receivedAt) {

  /**
   * Creates a response from a domain location.
   *
   * @param location domain location
   * @return API representation of the location
   */
  public static VehicleLocationResponse from(VehicleLocation location) {
    return new VehicleLocationResponse(
        location.id().value(),
        location.vehicleId().value(),
        location.latitude(),
        location.longitude(),
        location.speedKph(),
        location.heading(),
        location.accuracyMeters(),
        location.recordedAt(),
        location.receivedAt());
  }
}
