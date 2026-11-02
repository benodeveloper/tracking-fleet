package com.tracking.fleet.vehicle.infrastructure.persistence;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationId;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence model for a {@link VehicleLocation}.
 */
@Entity
@Table(name = "vehicle_locations")
public class VehicleLocationJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "vehicle_id", nullable = false, updatable = false)
  private UUID vehicleId;

  @Column(name = "latitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(name = "longitude", nullable = false, precision = 10, scale = 7)
  private BigDecimal longitude;

  @Column(name = "speed_kph", precision = 8, scale = 2)
  private BigDecimal speedKph;

  @Column(name = "heading", precision = 5, scale = 2)
  private BigDecimal heading;

  @Column(name = "accuracy_meters", precision = 8, scale = 2)
  private BigDecimal accuracyMeters;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt;

  @Column(name = "received_at", nullable = false, updatable = false)
  private Instant receivedAt;

  /**
   * Required by JPA.
   */
  protected VehicleLocationJpaEntity() {}

  private VehicleLocationJpaEntity(
      UUID id,
      UUID vehicleId,
      BigDecimal latitude,
      BigDecimal longitude,
      BigDecimal speedKph,
      BigDecimal heading,
      BigDecimal accuracyMeters,
      Instant recordedAt,
      Instant receivedAt) {
    this.id = id;
    this.vehicleId = vehicleId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.speedKph = speedKph;
    this.heading = heading;
    this.accuracyMeters = accuracyMeters;
    this.recordedAt = recordedAt;
    this.receivedAt = receivedAt;
  }

  /**
   * Creates a persistence entity from a domain location.
   *
   * @param location domain location
   * @return equivalent JPA entity
   */
  public static VehicleLocationJpaEntity from(VehicleLocation location) {
    return new VehicleLocationJpaEntity(
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

  /**
   * Rebuilds the domain location from persisted state.
   *
   * @return equivalent domain location
   */
  public VehicleLocation toDomain() {
    return VehicleLocation.restore(
        VehicleLocationId.of(id),
        VehicleId.of(vehicleId),
        latitude,
        longitude,
        speedKph,
        heading,
        accuracyMeters,
        recordedAt,
        receivedAt);
  }
}
