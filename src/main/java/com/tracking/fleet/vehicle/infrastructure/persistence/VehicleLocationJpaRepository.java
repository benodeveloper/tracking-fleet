package com.tracking.fleet.vehicle.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
   * Returns a page of location history for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @param from optional inclusive lower bound for the recorded timestamp
   * @param to optional inclusive upper bound for the recorded timestamp
   * @param pageable pagination and sorting information
   * @return matching locations
   */
  @Query(
      """
      select location
      from VehicleLocationJpaEntity location
      where location.vehicleId = :vehicleId
        and (:from is null or location.recordedAt >= :from)
        and (:to is null or location.recordedAt <= :to)
      """)
  Page<VehicleLocationJpaEntity> findHistory(
      @Param("vehicleId") UUID vehicleId,
      @Param("from") Instant from,
      @Param("to") Instant to,
      Pageable pageable);

  /**
   * Returns the latest location for every vehicle that has reported a position.
   *
   * @return latest locations, newest first
   */
  @Query(
      """
      select location
      from VehicleLocationJpaEntity location
      where location.recordedAt = (
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
