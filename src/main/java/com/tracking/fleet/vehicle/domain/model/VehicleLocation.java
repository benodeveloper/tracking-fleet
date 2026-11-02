package com.tracking.fleet.vehicle.domain.model;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A single GPS position reported by a vehicle.
 *
 * <p>Locations are immutable once recorded. The received timestamp is generated when the location
 * enters the system, while the recorded timestamp is supplied by the reporting device.
 */
public class VehicleLocation {

  private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
  private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
  private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
  private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);
  private static final BigDecimal MAX_HEADING = BigDecimal.valueOf(360);
  private static final long MAX_FUTURE_SKEW_SECONDS = 60;

  private final VehicleLocationId id;
  private final VehicleId vehicleId;
  private final BigDecimal latitude;
  private final BigDecimal longitude;
  private final BigDecimal speedKph;
  private final BigDecimal heading;
  private final BigDecimal accuracyMeters;
  private final Instant recordedAt;
  private final Instant receivedAt;

  private VehicleLocation(
      VehicleLocationId id,
      VehicleId vehicleId,
      BigDecimal latitude,
      BigDecimal longitude,
      BigDecimal speedKph,
      BigDecimal heading,
      BigDecimal accuracyMeters,
      Instant recordedAt,
      Instant receivedAt) {
    this.id = Objects.requireNonNull(id);
    this.vehicleId = Objects.requireNonNull(vehicleId);
    this.latitude = requireInRange(latitude, MIN_LATITUDE, MAX_LATITUDE, "Latitude");
    this.longitude = requireInRange(longitude, MIN_LONGITUDE, MAX_LONGITUDE, "Longitude");
    this.speedKph = validateNonNegative(speedKph, "Speed");
    this.heading = validateHeading(heading);
    this.accuracyMeters = validateNonNegative(accuracyMeters, "Accuracy");
    this.recordedAt = validateRecordedAt(recordedAt);
    this.receivedAt = Objects.requireNonNull(receivedAt);
  }

  /**
   * Records a new vehicle location.
   *
   * @param vehicleId vehicle that reported the location
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param speedKph optional speed in kilometres per hour
   * @param heading optional heading in degrees, from 0 inclusive to 360 exclusive
   * @param accuracyMeters optional horizontal accuracy in metres
   * @param recordedAt time at which the position was recorded by the device
   * @return a new location record
   * @throws IllegalArgumentException if any supplied value is invalid
   */
  public static VehicleLocation record(
      VehicleId vehicleId,
      BigDecimal latitude,
      BigDecimal longitude,
      BigDecimal speedKph,
      BigDecimal heading,
      BigDecimal accuracyMeters,
      Instant recordedAt) {
    return new VehicleLocation(
        VehicleLocationId.generate(),
        vehicleId,
        latitude,
        longitude,
        speedKph,
        heading,
        accuracyMeters,
        recordedAt,
        Instant.now());
  }

  /**
   * Reconstitutes a location from persisted state.
   *
   * @param id location identifier
   * @param vehicleId vehicle identifier
   * @param latitude latitude in decimal degrees
   * @param longitude longitude in decimal degrees
   * @param speedKph optional speed in kilometres per hour
   * @param heading optional heading in degrees
   * @param accuracyMeters optional horizontal accuracy in metres
   * @param recordedAt time at which the position was recorded by the device
   * @param receivedAt time at which the position entered the system
   * @return restored location
   */
  public static VehicleLocation restore(
      VehicleLocationId id,
      VehicleId vehicleId,
      BigDecimal latitude,
      BigDecimal longitude,
      BigDecimal speedKph,
      BigDecimal heading,
      BigDecimal accuracyMeters,
      Instant recordedAt,
      Instant receivedAt) {
    return new VehicleLocation(
        id, vehicleId, latitude, longitude, speedKph, heading, accuracyMeters, recordedAt, receivedAt);
  }

  private static BigDecimal requireInRange(
      BigDecimal value, BigDecimal minimum, BigDecimal maximum, String field) {
    Objects.requireNonNull(value, field + " cannot be null");

    if (value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
      throw new IllegalArgumentException(
          field + " must be between " + minimum + " and " + maximum);
    }

    return value;
  }

  private static BigDecimal validateNonNegative(BigDecimal value, String field) {
    if (value == null) {
      return null;
    }

    if (value.signum() < 0) {
      throw new IllegalArgumentException(field + " cannot be negative");
    }

    return value;
  }

  private static BigDecimal validateHeading(BigDecimal value) {
    if (value == null) {
      return null;
    }

    if (value.signum() < 0 || value.compareTo(MAX_HEADING) >= 0) {
      throw new IllegalArgumentException("Heading must be between 0 inclusive and 360 exclusive");
    }

    return value;
  }

  private static Instant validateRecordedAt(Instant recordedAt) {
    Objects.requireNonNull(recordedAt, "Recorded time cannot be null");

    if (recordedAt.isAfter(Instant.now().plusSeconds(MAX_FUTURE_SKEW_SECONDS))) {
      throw new IllegalArgumentException("Recorded time cannot be in the future");
    }

    return recordedAt;
  }

  /**
   * Returns the location identifier.
   *
   * @return location identifier
   */
  public VehicleLocationId id() {
    return id;
  }

  /**
   * Returns the vehicle that reported the location.
   *
   * @return vehicle identifier
   */
  public VehicleId vehicleId() {
    return vehicleId;
  }

  /**
   * Returns the latitude in decimal degrees.
   *
   * @return latitude
   */
  public BigDecimal latitude() {
    return latitude;
  }

  /**
   * Returns the longitude in decimal degrees.
   *
   * @return longitude
   */
  public BigDecimal longitude() {
    return longitude;
  }

  /**
   * Returns the reported speed, when available.
   *
   * @return speed in kilometres per hour or {@code null}
   */
  public BigDecimal speedKph() {
    return speedKph;
  }

  /**
   * Returns the reported heading, when available.
   *
   * @return heading in degrees or {@code null}
   */
  public BigDecimal heading() {
    return heading;
  }

  /**
   * Returns the reported horizontal accuracy, when available.
   *
   * @return accuracy in metres or {@code null}
   */
  public BigDecimal accuracyMeters() {
    return accuracyMeters;
  }

  /**
   * Returns the time at which the device recorded the position.
   *
   * @return recorded timestamp
   */
  public Instant recordedAt() {
    return recordedAt;
  }

  /**
   * Returns the time at which the position entered the system.
   *
   * @return received timestamp
   */
  public Instant receivedAt() {
    return receivedAt;
  }
}
