package com.tracking.fleet.vehicle.application;

import com.tracking.fleet.vehicle.domain.model.Vehicle;
import java.util.List;

/**
 * One page of vehicles together with pagination metadata.
 *
 * @param vehicles vehicles on the current page
 * @param page zero-based page index
 * @param size requested page size
 * @param totalElements total number of vehicles
 */
public record VehiclePage(List<Vehicle> vehicles, int page, int size, long totalElements) {

  /**
   * Creates a page and defensively copies its content.
   */
  public VehiclePage {
    vehicles = List.copyOf(vehicles);
  }
}
