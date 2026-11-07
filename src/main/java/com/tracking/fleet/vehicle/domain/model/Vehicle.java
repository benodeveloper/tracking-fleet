package com.tracking.fleet.vehicle.domain.model;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a vehicle in the fleet.
 *
 * <p>A vehicle owns its identity, registration data, specification, lifecycle status and mileage.
 * State changes are validated by the aggregate so that invalid transitions and mileage rollbacks
 * cannot be persisted.
 *
 * <p>Instances are created with {@link #register(RegistrationNumber, String,
 * VehicleSpecification, String)} or reconstituted from persistence with {@link #restore}.
 */
public class Vehicle {

  private final VehicleId id;
  private RegistrationNumber registrationNumber;
  private String vin;
  private VehicleSpecification specification;

  private String color;

  private VehicleStatus status;
  private BigDecimal mileage;

  private final Instant createdAt;
  private Instant updatedAt;
  private Instant deletedAt;

  private Vehicle(
      VehicleId id,
      RegistrationNumber registrationNumber,
      String vin,
      VehicleSpecification specification,
      String color,
      VehicleStatus status,
      BigDecimal mileage,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    this.id = Objects.requireNonNull(id);
    this.registrationNumber = Objects.requireNonNull(registrationNumber);
    this.vin = validateVin(vin);
    this.specification = Objects.requireNonNull(specification);
    this.color = normalizeOptionalText(color);
    this.status = Objects.requireNonNull(status);
    this.mileage = validateMileage(mileage);
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);
    this.deletedAt = deletedAt;
  }

  /**
   * Registers a new vehicle.
   *
   * <p>A newly registered vehicle is active and has zero mileage.
   *
   * @param registrationNumber unique registration number
   * @param vin optional vehicle identification number; blank values are treated as absent
   * @param specification vehicle specification
   * @param color optional vehicle color
   * @return a new active vehicle
   * @throws IllegalArgumentException if the VIN is invalid
   */
  public static Vehicle register(
      RegistrationNumber registrationNumber,
      String vin,
      VehicleSpecification specification,
      String color) {
    Instant now = Instant.now();

    return new Vehicle(
        VehicleId.generate(),
        registrationNumber,
        vin,
        specification,
        color,
        VehicleStatus.ACTIVE,
        BigDecimal.ZERO,
        now,
        now,
        null);
  }

  /**
   * Reconstitutes a vehicle from persisted state.
   *
   * <p>This factory is intended for persistence adapters. Application code should use {@link
   * #register(RegistrationNumber, String, VehicleSpecification, String)} instead.
   *
   * @param id vehicle identifier
   * @param registrationNumber registration number
   * @param vin optional VIN
   * @param specification vehicle specification
   * @param color optional color
   * @param status current lifecycle status
   * @param mileage current mileage
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @return restored vehicle
   */
  public static Vehicle restore(
      VehicleId id,
      RegistrationNumber registrationNumber,
      String vin,
      VehicleSpecification specification,
      String color,
      VehicleStatus status,
      BigDecimal mileage,
      Instant createdAt,
      Instant updatedAt) {
    return restore(
        id,
        registrationNumber,
        vin,
        specification,
        color,
        status,
        mileage,
        createdAt,
        updatedAt,
        null);
  }

  /**
   * Reconstitutes a vehicle from persisted state, including its soft-delete timestamp.
   *
   * <p>This factory is intended for persistence adapters. Application code should use {@link
   * #register(RegistrationNumber, String, VehicleSpecification, String)} instead.
   *
   * @param id vehicle identifier
   * @param registrationNumber registration number
   * @param vin optional VIN
   * @param specification vehicle specification
   * @param color optional color
   * @param status current lifecycle status
   * @param mileage current mileage
   * @param createdAt creation timestamp
   * @param updatedAt last update timestamp
   * @param deletedAt soft-delete timestamp, or {@code null} when the vehicle is active
   * @return restored vehicle
   */
  public static Vehicle restore(
      VehicleId id,
      RegistrationNumber registrationNumber,
      String vin,
      VehicleSpecification specification,
      String color,
      VehicleStatus status,
      BigDecimal mileage,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    return new Vehicle(
        id,
        registrationNumber,
        vin,
        specification,
        color,
        status,
        mileage,
        createdAt,
        updatedAt,
        deletedAt);
  }

  /**
   * Updates the editable descriptive data of the vehicle.
   *
   * <p>Identity, lifecycle status and mileage are not changed by this method.
   *
   * @param registrationNumber new registration number
   * @param vin new optional VIN
   * @param specification new vehicle specification
   * @param color new optional color
   * @throws IllegalArgumentException if the VIN is invalid
   */
  public void updateDetails(
      RegistrationNumber registrationNumber,
      String vin,
      VehicleSpecification specification,
      String color) {
    this.registrationNumber = Objects.requireNonNull(registrationNumber);
    this.vin = validateVin(vin);
    this.specification = Objects.requireNonNull(specification);
    this.color = normalizeOptionalText(color);
    this.updatedAt = Instant.now();
  }

  /**
   * Marks the vehicle as active.
   *
   * @throws IllegalStateException if the vehicle is retired or currently under maintenance
   */
  public void activate() {
    if (status == VehicleStatus.RETIRED) {
      throw new IllegalStateException("A retired vehicle cannot be activated");
    }

    if (status == VehicleStatus.UNDER_MAINTENANCE) {
      throw new IllegalStateException("A vehicle under maintenance cannot be activated");
    }

    changeStatus(VehicleStatus.ACTIVE);
  }

  /**
   * Marks the vehicle as inactive.
   *
   * @throws IllegalStateException if the vehicle is retired or currently under maintenance
   */
  public void deactivate() {
    if (status == VehicleStatus.RETIRED) {
      throw new IllegalStateException("A retired vehicle cannot be deactivated");
    }

    if (status == VehicleStatus.UNDER_MAINTENANCE) {
      throw new IllegalStateException("A vehicle under maintenance cannot be deactivated");
    }

    changeStatus(VehicleStatus.INACTIVE);
  }

  /**
   * Sends the vehicle to maintenance.
   *
   * @throws IllegalStateException if the vehicle is retired
   */
  public void sendToMaintenance() {
    if (status == VehicleStatus.RETIRED) {
      throw new IllegalStateException("A retired vehicle cannot enter maintenance");
    }

    changeStatus(VehicleStatus.UNDER_MAINTENANCE);
  }

  /**
   * Completes maintenance and returns the vehicle to active service.
   *
   * @throws IllegalStateException if the vehicle is not currently under maintenance
   */
  public void completeMaintenance() {
    if (status != VehicleStatus.UNDER_MAINTENANCE) {
      throw new IllegalStateException("Vehicle is not currently under maintenance");
    }

    changeStatus(VehicleStatus.ACTIVE);
  }

  /**
   * Retires the vehicle from the fleet.
   *
   * <p>Retirement is terminal: a retired vehicle cannot be activated, deactivated or sent to
   * maintenance again.
   */
  public void retire() {
    changeStatus(VehicleStatus.RETIRED);
  }

  /**
   * Transitions the vehicle to the requested lifecycle status.
   *
   * <p>Completing maintenance is handled automatically when the current status is {@link
   * VehicleStatus#UNDER_MAINTENANCE} and the target status is {@link VehicleStatus#ACTIVE}.
   *
   * @param targetStatus desired status, must not be {@code null}
   * @throws NullPointerException if {@code targetStatus} is {@code null}
   * @throws IllegalStateException if the transition is not allowed
   */
  public void transitionTo(VehicleStatus targetStatus) {
    Objects.requireNonNull(targetStatus, "Vehicle status cannot be null");

    if (status == targetStatus) {
      return;
    }

    if (status == VehicleStatus.UNDER_MAINTENANCE && targetStatus == VehicleStatus.ACTIVE) {
      completeMaintenance();
      return;
    }

    switch (targetStatus) {
      case ACTIVE -> activate();
      case INACTIVE -> deactivate();
      case UNDER_MAINTENANCE -> sendToMaintenance();
      case RETIRED -> retire();
    }
  }

  /**
   * Updates the vehicle mileage.
   *
   * @param newMileage new mileage, which cannot be lower than the current mileage
   * @throws NullPointerException if {@code newMileage} is {@code null}
   * @throws IllegalArgumentException if the mileage is negative or lower than the current mileage
   */
  public void updateMileage(BigDecimal newMileage) {
    Objects.requireNonNull(newMileage, "Mileage cannot be null");

    if (newMileage.compareTo(mileage) < 0) {
      throw new IllegalArgumentException("New mileage cannot be lower than current mileage");
    }

    this.mileage = validateMileage(newMileage);
    this.updatedAt = Instant.now();
  }

  /**
   * Marks the vehicle as deleted while retaining its persisted identity and history.
   *
   * <p>Repeated calls keep the original deletion timestamp unchanged.
   */
  public void delete() {
    if (deletedAt != null) {
      return;
    }

    Instant now = Instant.now();
    this.deletedAt = now;
    this.updatedAt = now;
  }

  private void changeStatus(VehicleStatus newStatus) {
    if (this.status == newStatus) {
      return;
    }

    this.status = newStatus;
    this.updatedAt = Instant.now();
  }

  private static BigDecimal validateMileage(BigDecimal mileage) {
    Objects.requireNonNull(mileage, "Mileage cannot be null");

    if (mileage.signum() < 0) {
      throw new IllegalArgumentException("Mileage cannot be negative");
    }

    return mileage;
  }

  private static String validateVin(String vin) {
    if (vin == null || vin.isBlank()) {
      return null;
    }

    String normalized = vin.trim().toUpperCase();

    if (!normalized.matches("^[A-HJ-NPR-Z0-9]{17}$")) {
      throw new IllegalArgumentException("Invalid VIN");
    }

    return normalized;
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  // Read-only accessors

  /**
   * Returns the vehicle identifier.
   *
   * @return vehicle identifier
   */
  public VehicleId id() {
    return id;
  }

  /**
   * Returns the registration number.
   *
   * @return registration number
   */
  public RegistrationNumber registrationNumber() {
    return registrationNumber;
  }

  /**
   * Returns the vehicle identification number, when present.
   *
   * @return VIN or {@code null}
   */
  public String vin() {
    return vin;
  }

  /**
   * Returns the vehicle specification.
   *
   * @return specification
   */
  public VehicleSpecification specification() {
    return specification;
  }

  /**
   * Returns the vehicle color, when present.
   *
   * @return color or {@code null}
   */
  public String color() {
    return color;
  }

  /**
   * Returns the current lifecycle status.
   *
   * @return status
   */
  public VehicleStatus status() {
    return status;
  }

  /**
   * Returns the current mileage.
   *
   * @return mileage
   */
  public BigDecimal mileage() {
    return mileage;
  }

  /**
   * Returns the creation timestamp.
   *
   * @return creation timestamp
   */
  public Instant createdAt() {
    return createdAt;
  }

  /**
   * Returns the last update timestamp.
   *
   * @return last update timestamp
   */
  public Instant updatedAt() {
    return updatedAt;
  }

  /**
   * Returns whether the vehicle has been soft-deleted.
   *
   * @return {@code true} when the vehicle is soft-deleted
   */
  public boolean isDeleted() {
    return deletedAt != null;
  }

  /**
   * Returns the soft-delete timestamp, when the vehicle has been deleted.
   *
   * @return deletion timestamp or {@code null}
   */
  public Instant deletedAt() {
    return deletedAt;
  }
}
