package com.tracking.fleet.vehicle.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository used by {@link VehicleRepositoryAdapter}.
 */
interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {

  /**
   * Checks whether a registration number is already stored.
   *
   * @param registrationNumber registration number
   * @return {@code true} when the registration number exists
   */
  boolean existsByRegistrationNumber(String registrationNumber);

  /**
   * Checks whether a VIN is already stored.
   *
   * @param vin VIN
   * @return {@code true} when the VIN exists
   */
  boolean existsByVin(String vin);
}
