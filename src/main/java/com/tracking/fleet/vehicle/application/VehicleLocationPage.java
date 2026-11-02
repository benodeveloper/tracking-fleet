package com.tracking.fleet.vehicle.application;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.util.List;

/**
 * One page of vehicle locations together with pagination metadata.
 *
 * @param locations locations on the current page
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total number of matching locations
 */
public record VehicleLocationPage(
    List<VehicleLocation> locations, int page, int size, long totalElements) {

  /**
   * Creates a page and defensively copies its content.
   */
  public VehicleLocationPage {
    locations = List.copyOf(locations);
  }
}
