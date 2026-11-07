package com.tracking.fleet.vehicle.api.dto;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "VehicleLocationResponse", description = "A recorded vehicle GPS position")
public record VehicleLocationResponse(
    @Schema(
            description = "Location identifier",
            example = "9b2f1c4e-7a10-4f8c-9c42-21d2d6a3e8f1",
            accessMode = Schema.AccessMode.READ_ONLY)
        UUID id,
    @Schema(
            description = "Vehicle identifier",
            example = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
            accessMode = Schema.AccessMode.READ_ONLY)
        UUID vehicleId,
    @Schema(description = "Latitude in decimal degrees", example = "52.3676") BigDecimal latitude,
    @Schema(description = "Longitude in decimal degrees", example = "4.9041") BigDecimal longitude,
    @Schema(description = "Speed in kilometres per hour", example = "42.5") BigDecimal speedKph,
    @Schema(description = "Heading in degrees", example = "90") BigDecimal heading,
    @Schema(description = "Horizontal accuracy in metres", example = "4.0")
        BigDecimal accuracyMeters,
    @Schema(
            description = "Time at which the device recorded the position",
            example = "2026-01-01T10:00:00Z")
        Instant recordedAt,
    @Schema(
            description = "Time at which the position entered the system",
            example = "2026-01-01T10:00:01Z",
            accessMode = Schema.AccessMode.READ_ONLY)
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
