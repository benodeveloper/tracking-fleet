package com.tracking.fleet.vehicle.domain;

import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link Vehicle} aggregates.
 *
 * <p>Implementations live in the infrastructure layer and handle persistence concerns.
 */
public interface VehicleRepository {

  /**
   * Persists a vehicle.
   *
   * @param vehicle vehicle to save
   * @return the saved vehicle
   */
  Vehicle save(Vehicle vehicle);

  /**
   * Finds an active vehicle by its identifier.
   *
   * <p>Soft-deleted vehicles are excluded.
   *
   * @param id vehicle identifier
   * @return the vehicle when found and not soft-deleted
   */
  Optional<Vehicle> findById(VehicleId id);

  /**
   * Returns one page of active vehicles, newest first.
   *
   * <p>Soft-deleted vehicles are excluded.
   *
   * @param page zero-based page index
   * @param size maximum number of vehicles to return
   * @return vehicles on the requested page
   */
  List<Vehicle> findAll(int page, int size);

  /**
   * Counts active vehicles.
   *
   * <p>Soft-deleted vehicles are excluded.
   *
   * @return total number of active vehicles
   */
  long count();

  /**
   * Checks whether an active vehicle already uses a registration number.
   *
   * @param registrationNumber registration number to check
   * @return {@code true} when an active vehicle already uses the registration number
   */
  boolean existsByRegistrationNumber(RegistrationNumber registrationNumber);

  /**
   * Checks whether an active vehicle already uses a VIN.
   *
   * @param vin VIN to check
   * @return {@code true} when an active vehicle already uses the VIN
   */
  boolean existsByVin(String vin);
}
