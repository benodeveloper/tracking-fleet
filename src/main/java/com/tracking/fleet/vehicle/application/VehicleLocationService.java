package com.tracking.fleet.vehicle.application;

import com.tracking.fleet.vehicle.application.command.RecordVehicleLocationCommand;
import com.tracking.fleet.vehicle.application.exception.VehicleLocationNotFoundException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationRepository;
import com.tracking.fleet.vehicle.domain.VehicleRepository;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for recording and querying vehicle locations.
 */
@Service
@Transactional
public class VehicleLocationService {

  private final VehicleRepository vehicleRepository;
  private final VehicleLocationRepository vehicleLocationRepository;

  /**
   * Creates the vehicle location service.
   *
   * @param vehicleRepository vehicle repository port
   * @param vehicleLocationRepository location repository port
   */
  public VehicleLocationService(
      VehicleRepository vehicleRepository, VehicleLocationRepository vehicleLocationRepository) {
    this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
    this.vehicleLocationRepository = Objects.requireNonNull(vehicleLocationRepository);
  }

  /**
   * Records a position for an existing vehicle.
   *
   * @param vehicleId vehicle identifier
   * @param command location data
   * @return the recorded location
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws IllegalArgumentException if the location data is invalid
   */
  public VehicleLocation record(VehicleId vehicleId, RecordVehicleLocationCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");
    requireVehicleExists(vehicleId);

    VehicleLocation location =
        VehicleLocation.record(
            vehicleId,
            command.latitude(),
            command.longitude(),
            command.speedKph(),
            command.heading(),
            command.accuracyMeters(),
            command.recordedAt());

    return vehicleLocationRepository.save(location);
  }

  /**
   * Returns the latest known position of a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @return the latest location
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws VehicleLocationNotFoundException if the vehicle has not reported a location
   */
  @Transactional(readOnly = true)
  public VehicleLocation getLatest(VehicleId vehicleId) {
    requireVehicleExists(vehicleId);

    return vehicleLocationRepository
        .findLatestByVehicleId(vehicleId)
        .orElseThrow(() -> new VehicleLocationNotFoundException(vehicleId));
  }

  /**
   * Returns a page of a vehicle's location history.
   *
   * @param vehicleId vehicle identifier
   * @param from optional inclusive lower bound for the recorded timestamp
   * @param to optional inclusive upper bound for the recorded timestamp
   * @param page zero-based page index
   * @param size page size, between 1 and 100
   * @return the requested history page
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws IllegalArgumentException if the pagination or time range is invalid
   */
  @Transactional(readOnly = true)
  public VehicleLocationPage findHistory(
      VehicleId vehicleId, Instant from, Instant to, int page, int size) {
    requireVehicleExists(vehicleId);
    validatePagination(page, size);

    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("'from' must not be after 'to'");
    }

    var history = vehicleLocationRepository.findHistory(vehicleId, from, to, page, size);

    return new VehicleLocationPage(history.locations(), page, size, history.totalElements());
  }

  /**
   * Returns the latest reported location for every vehicle.
   *
   * @return latest locations, newest first
   */
  @Transactional(readOnly = true)
  public List<VehicleLocation> findLive() {
    return vehicleLocationRepository.findLatestForAllVehicles();
  }

  private void requireVehicleExists(VehicleId vehicleId) {
    Objects.requireNonNull(vehicleId, "Vehicle ID cannot be null");

    if (vehicleRepository.findById(vehicleId).isEmpty()) {
      throw new VehicleNotFoundException(vehicleId);
    }
  }

  private static void validatePagination(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page index cannot be negative");
    }

    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
  }
}
