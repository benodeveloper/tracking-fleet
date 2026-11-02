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
   * Finds a vehicle by its identifier.
   *
   * @param id vehicle identifier
   * @return the vehicle when found
   */
  Optional<Vehicle> findById(VehicleId id);

  /**
   * Returns one page of vehicles, newest first.
   *
   * @param page zero-based page index
   * @param size maximum number of vehicles to return
   * @return vehicles on the requested page
   */
  List<Vehicle> findAll(int page, int size);

  /**
   * Counts all stored vehicles.
   *
   * @return total number of vehicles
   */
  long count();

  /**
   * Checks whether a registration number is already in use.
   *
   * @param registrationNumber registration number to check
   * @return {@code true} when a vehicle already uses the registration number
   */
  boolean existsByRegistrationNumber(RegistrationNumber registrationNumber);

  /**
   * Checks whether a VIN is already in use.
   *
   * @param vin VIN to check
   * @return {@code true} when a vehicle already uses the VIN
   */
  boolean existsByVin(String vin);

  /**
   * Deletes a vehicle.
   *
   * @param vehicle vehicle to delete
   */
  void delete(Vehicle vehicle);
}
