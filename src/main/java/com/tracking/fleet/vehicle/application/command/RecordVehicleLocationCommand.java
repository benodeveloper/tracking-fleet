package com.tracking.fleet.vehicle.application.command;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Command for recording a vehicle location.
 *
 * @param latitude latitude in decimal degrees
 * @param longitude longitude in decimal degrees
 * @param speedKph optional speed in kilometres per hour
 * @param heading optional heading in degrees
 * @param accuracyMeters optional horizontal accuracy in metres
 * @param recordedAt time at which the device recorded the position
 */
public record RecordVehicleLocationCommand(
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal speedKph,
    BigDecimal heading,
    BigDecimal accuracyMeters,
    Instant recordedAt) {}
