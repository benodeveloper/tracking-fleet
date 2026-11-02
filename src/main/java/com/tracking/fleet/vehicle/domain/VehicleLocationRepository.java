package com.tracking.fleet.vehicle.domain;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository port for {@link VehicleLocation} records.
 */
public interface VehicleLocationRepository {

  /**
   * Persists a vehicle location.
   *
   * @param location location to save
   * @return the saved location
   */
  VehicleLocation save(VehicleLocation location);

  /**
   * Returns the most recently recorded location for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @return the latest location when one exists
   */
  Optional<VehicleLocation> findLatestByVehicleId(VehicleId vehicleId);

  /**
   * Returns a page of a vehicle's location history, newest first.
   *
   * @param vehicleId vehicle identifier
   * @param from optional inclusive lower bound for the recorded timestamp
   * @param to optional inclusive upper bound for the recorded timestamp
   * @param page zero-based page index
   * @param size maximum number of locations to return
   * @return matching locations and the total count
   */
  VehicleLocationHistory findHistory(
      VehicleId vehicleId, Instant from, Instant to, int page, int size);

  /**
   * Returns the latest location for every vehicle that has reported a position.
   *
   * @return latest locations, newest first
   */
  List<VehicleLocation> findLatestForAllVehicles();
}
