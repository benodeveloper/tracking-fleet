package com.tracking.fleet.vehicle.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data repository used by {@link VehicleRepositoryAdapter}.
 */
interface VehicleJpaRepository extends JpaRepository<VehicleJpaEntity, UUID> {

  /**
   * Finds a vehicle by identifier, excluding soft-deleted vehicles.
   *
   * @param id vehicle identifier
   * @return the vehicle when it exists and is active
   */
  Optional<VehicleJpaEntity> findByIdAndDeletedAtIsNull(UUID id);

  /**
   * Returns a page of active vehicles.
   *
   * @param pageable pagination and sorting information
   * @return active vehicles
   */
  Page<VehicleJpaEntity> findAllByDeletedAtIsNull(Pageable pageable);

  /**
   * Counts active vehicles.
   *
   * @return number of active vehicles
   */
  long countByDeletedAtIsNull();

  /**
   * Checks whether an active vehicle already uses a registration number.
   *
   * @param registrationNumber registration number
   * @return {@code true} when an active vehicle uses the registration number
   */
  boolean existsByRegistrationNumberAndDeletedAtIsNull(String registrationNumber);

  /**
   * Checks whether an active vehicle already uses a VIN.
   *
   * @param vin VIN
   * @return {@code true} when an active vehicle uses the VIN
   */
  boolean existsByVinAndDeletedAtIsNull(String vin);
}
