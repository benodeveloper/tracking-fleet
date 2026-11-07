package com.tracking.fleet.vehicle.infrastructure.persistence;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationHistory;
import com.tracking.fleet.vehicle.domain.VehicleLocationRepository;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Persistence adapter that implements the domain {@link VehicleLocationRepository} port with
 * Spring Data JPA.
 */
@Repository
public class VehicleLocationRepositoryAdapter implements VehicleLocationRepository {

  private final VehicleLocationJpaRepository jpaRepository;

  /**
   * Creates the location persistence adapter.
   *
   * @param jpaRepository Spring Data repository
   */
  public VehicleLocationRepositoryAdapter(VehicleLocationJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public VehicleLocation save(VehicleLocation location) {
    return jpaRepository.saveAndFlush(VehicleLocationJpaEntity.from(location)).toDomain();
  }

  @Override
  public Optional<VehicleLocation> findLatestByVehicleId(VehicleId vehicleId) {
    return jpaRepository
        .findFirstByVehicleIdOrderByRecordedAtDescReceivedAtDesc(vehicleId.value())
        .map(VehicleLocationJpaEntity::toDomain);
  }

  @Override
  public VehicleLocationHistory findHistory(
      VehicleId vehicleId, Instant from, Instant to, int page, int size) {
    var pageable =
        PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "recordedAt")
                .and(Sort.by(Sort.Direction.DESC, "receivedAt")));

    var result = findPage(jpaRepository, vehicleId.value(), from, to, pageable);
    var locations = result.map(VehicleLocationJpaEntity::toDomain).getContent();

    return new VehicleLocationHistory(locations, result.getTotalElements());
  }

  private static Page<VehicleLocationJpaEntity> findPage(
      VehicleLocationJpaRepository jpaRepository,
      UUID vehicleId,
      Instant from,
      Instant to,
      PageRequest pageable) {
    if (from == null && to == null) {
      return jpaRepository.findByVehicleId(vehicleId, pageable);
    }

    if (to == null) {
      return jpaRepository.findByVehicleIdAndRecordedAtGreaterThanEqual(
          vehicleId, from, pageable);
    }

    if (from == null) {
      return jpaRepository.findByVehicleIdAndRecordedAtLessThanEqual(vehicleId, to, pageable);
    }

    return jpaRepository.findByVehicleIdAndRecordedAtBetween(vehicleId, from, to, pageable);
  }

  @Override
  public List<VehicleLocation> findLatestForAllVehicles() {
    return jpaRepository.findLatestForAllVehicles().stream()
        .map(VehicleLocationJpaEntity::toDomain)
        .toList();
  }
}
