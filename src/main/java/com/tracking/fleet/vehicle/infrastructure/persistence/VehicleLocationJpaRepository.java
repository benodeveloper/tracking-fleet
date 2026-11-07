package com.tracking.fleet.vehicle.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data repository used by {@link VehicleLocationRepositoryAdapter}.
 */
interface VehicleLocationJpaRepository extends JpaRepository<VehicleLocationJpaEntity, UUID> {

  /**
   * Returns the most recently recorded location for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @return the latest location when one exists
   */
  Optional<VehicleLocationJpaEntity>
      findFirstByVehicleIdOrderByRecordedAtDescReceivedAtDesc(UUID vehicleId);

  /**
   * Returns a page of all locations for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @param pageable pagination and sorting information
   * @return matching locations
   */
  Page<VehicleLocationJpaEntity> findByVehicleId(UUID vehicleId, Pageable pageable);

  /**
   * Returns a page of locations recorded at or after the supplied time.
   *
   * @param vehicleId vehicle identifier
   * @param from inclusive lower bound for the recorded timestamp
   * @param pageable pagination and sorting information
   * @return matching locations
   */
  Page<VehicleLocationJpaEntity> findByVehicleIdAndRecordedAtGreaterThanEqual(
      UUID vehicleId, Instant from, Pageable pageable);

  /**
   * Returns a page of locations recorded at or before the supplied time.
   *
   * @param vehicleId vehicle identifier
   * @param to inclusive upper bound for the recorded timestamp
   * @param pageable pagination and sorting information
   * @return matching locations
   */
  Page<VehicleLocationJpaEntity> findByVehicleIdAndRecordedAtLessThanEqual(
      UUID vehicleId, Instant to, Pageable pageable);

  /**
   * Returns a page of locations recorded within the supplied time range.
   *
   * @param vehicleId vehicle identifier
   * @param from inclusive lower bound for the recorded timestamp
   * @param to inclusive upper bound for the recorded timestamp
   * @param pageable pagination and sorting information
   * @return matching locations
   */
  Page<VehicleLocationJpaEntity> findByVehicleIdAndRecordedAtBetween(
      UUID vehicleId, Instant from, Instant to, Pageable pageable);

  /**
   * Returns the latest location for every vehicle that has reported a position.
   *
   * @return latest locations, newest first
   */
  @Query(
      """
      select location
      from VehicleLocationJpaEntity location, VehicleJpaEntity vehicle
      where location.vehicleId = vehicle.id
      and vehicle.deletedAt is null
      and location.recordedAt = (
        select max(latestRecorded.recordedAt)
        from VehicleLocationJpaEntity latestRecorded
        where latestRecorded.vehicleId = location.vehicleId
      )
      and location.receivedAt = (
        select max(latestReceived.receivedAt)
        from VehicleLocationJpaEntity latestReceived
        where latestReceived.vehicleId = location.vehicleId
          and latestReceived.recordedAt = location.recordedAt
      )
      order by location.recordedAt desc
      """)
  List<VehicleLocationJpaEntity> findLatestForAllVehicles();
}
