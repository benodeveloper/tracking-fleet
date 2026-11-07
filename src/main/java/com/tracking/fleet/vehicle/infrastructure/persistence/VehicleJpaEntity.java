package com.tracking.fleet.vehicle.infrastructure.persistence;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA persistence model for the {@link Vehicle} aggregate.
 *
 * <p>The entity mirrors the {@code vehicles} table created by Flyway and is converted to and from
 * the domain model at the persistence boundary.
 */
@Entity
@Table(name = "vehicles")
public class VehicleJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "registration_number", nullable = false, length = 30)
  private String registrationNumber;

  @Column(name = "vin", length = 17)
  private String vin;

  @Column(name = "brand", nullable = false, length = 100)
  private String brand;

  @Column(name = "model", nullable = false, length = 100)
  private String model;

  @Column(name = "manufacturing_year", nullable = false)
  private short manufacturingYear;

  @Enumerated(EnumType.STRING)
  @Column(name = "vehicle_type", nullable = false, length = 30)
  private VehicleType vehicleType;

  @Enumerated(EnumType.STRING)
  @Column(name = "fuel_type", nullable = false, length = 30)
  private FuelType fuelType;

  @Enumerated(EnumType.STRING)
  @Column(name = "transmission_type", nullable = false, length = 30)
  private TransmissionType transmissionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private VehicleStatus status;

  @Column(name = "color", length = 50)
  private String color;

  @Column(name = "mileage", nullable = false, precision = 12, scale = 2)
  private BigDecimal mileage;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  /**
   * Required by JPA.
   */
  protected VehicleJpaEntity() {}

  private VehicleJpaEntity(
      UUID id,
      String registrationNumber,
      String vin,
      String brand,
      String model,
      short manufacturingYear,
      VehicleType vehicleType,
      FuelType fuelType,
      TransmissionType transmissionType,
      VehicleStatus status,
      String color,
      BigDecimal mileage,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    this.id = id;
    this.registrationNumber = registrationNumber;
    this.vin = vin;
    this.brand = brand;
    this.model = model;
    this.manufacturingYear = manufacturingYear;
    this.vehicleType = vehicleType;
    this.fuelType = fuelType;
    this.transmissionType = transmissionType;
    this.status = status;
    this.color = color;
    this.mileage = mileage;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  /**
   * Creates a persistence entity from a domain vehicle.
   *
   * @param vehicle domain vehicle
   * @return equivalent JPA entity
   */
  public static VehicleJpaEntity from(Vehicle vehicle) {
    VehicleSpecification specification = vehicle.specification();

    return new VehicleJpaEntity(
        vehicle.id().value(),
        vehicle.registrationNumber().value(),
        vehicle.vin(),
        specification.brand(),
        specification.model(),
        (short) specification.manufacturingYear(),
        specification.type(),
        specification.fuelType(),
        specification.transmissionType(),
        vehicle.status(),
        vehicle.color(),
        vehicle.mileage(),
        vehicle.createdAt(),
        vehicle.updatedAt(),
        vehicle.deletedAt());
  }

  /**
   * Rebuilds the domain vehicle from persisted state.
   *
   * @return equivalent domain vehicle
   */
  public Vehicle toDomain() {
    return Vehicle.restore(
        VehicleId.of(id),
        new RegistrationNumber(registrationNumber),
        vin,
        new VehicleSpecification(
            brand, model, manufacturingYear, vehicleType, fuelType, transmissionType),
        color,
        status,
        mileage,
        createdAt,
        updatedAt,
        deletedAt);
  }
}
