package com.tracking.fleet.driver.api.dto;

import com.tracking.fleet.vehicle.domain.model.Vehicle;
import java.util.UUID;

public record DriverVehicleResponse(
    UUID id,
    String registrationNumber,
    String brand,
    String model,
    String status) {

  public static DriverVehicleResponse from(Vehicle v) {
    return new DriverVehicleResponse(
        v.id().value(), v.registrationNumber().value(), v.specification().brand(),
        v.specification().model(), v.status().name());
  }
}
