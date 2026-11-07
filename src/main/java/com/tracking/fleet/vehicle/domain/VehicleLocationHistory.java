package com.tracking.fleet.vehicle.domain;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.util.List;

/**
 * One page of a vehicle's location history.
 *
 * @param locations locations on the current page, newest first
 * @param totalElements total number of locations matching the query
 */
public record VehicleLocationHistory(List<VehicleLocation> locations, long totalElements) {

  /**
   * Creates a history page and defensively copies its content.
   */
  public VehicleLocationHistory {
    locations = List.copyOf(locations);
  }
}
