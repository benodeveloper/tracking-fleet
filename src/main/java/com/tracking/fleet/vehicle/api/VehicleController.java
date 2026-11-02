package com.tracking.fleet.vehicle.api;

import com.tracking.fleet.shared.api.response.ApiResponse;
import com.tracking.fleet.shared.api.response.PageResponse;
import com.tracking.fleet.vehicle.api.dto.CreateVehicleRequest;
import com.tracking.fleet.vehicle.api.dto.UpdateVehicleMileageRequest;
import com.tracking.fleet.vehicle.api.dto.UpdateVehicleRequest;
import com.tracking.fleet.vehicle.api.dto.UpdateVehicleStatusRequest;
import com.tracking.fleet.vehicle.api.dto.VehicleResponse;
import com.tracking.fleet.vehicle.application.VehiclePage;
import com.tracking.fleet.vehicle.application.VehicleService;
import com.tracking.fleet.vehicle.application.command.CreateVehicleCommand;
import com.tracking.fleet.vehicle.application.command.UpdateVehicleCommand;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for vehicle CRUD operations.
 *
 * <p>All responses are wrapped in {@link com.tracking.fleet.shared.api.response.ApiResponse}.
 */
@RequestMapping("/api/v1/vehicles")
@RestController
public class VehicleController {

  private final VehicleService vehicleService;

  /**
   * Creates the vehicle REST controller.
   *
   * @param vehicleService vehicle application service
   */
  public VehicleController(VehicleService vehicleService) {
    this.vehicleService = vehicleService;
  }

  /**
   * Creates a new vehicle.
   *
   * @param request vehicle data
   * @return the created vehicle with HTTP 201
   */
  @PostMapping
  public ResponseEntity<ApiResponse<VehicleResponse>> create(
      @Valid @RequestBody CreateVehicleRequest request) {
    Vehicle vehicle =
        vehicleService.create(
            new CreateVehicleCommand(
                request.registrationNumber(),
                request.vin(),
                request.brand(),
                request.model(),
                request.manufacturingYear(),
                request.type(),
                request.fuelType(),
                request.transmissionType(),
                request.color()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Vehicle created successfully", VehicleResponse.from(vehicle)));
  }

  /**
   * Returns a vehicle by its identifier.
   *
   * @param id vehicle identifier
   * @return the requested vehicle
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
    Vehicle vehicle = vehicleService.get(VehicleId.of(id));

    return ResponseEntity.ok(
        ApiResponse.success("Vehicle retrieved successfully", VehicleResponse.from(vehicle)));
  }

  /**
   * Returns a page of vehicles.
   *
   * @param page zero-based page index
   * @param size page size
   * @return the requested page
   */
  @GetMapping
  public ResponseEntity<ApiResponse<PageResponse<VehicleResponse>>> getAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    VehiclePage vehicles = vehicleService.findAll(page, size);

    var content = vehicles.vehicles().stream().map(VehicleResponse::from).toList();
    var pageResponse =
        PageResponse.of(content, vehicles.page(), vehicles.size(), vehicles.totalElements());

    return ResponseEntity.ok(
        ApiResponse.success("Vehicles retrieved successfully", pageResponse));
  }

  /**
   * Replaces the descriptive data of a vehicle.
   *
   * @param id vehicle identifier
   * @param request new vehicle data
   * @return the updated vehicle
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<VehicleResponse>> update(
      @PathVariable UUID id, @Valid @RequestBody UpdateVehicleRequest request) {
    Vehicle vehicle =
        vehicleService.update(
            VehicleId.of(id),
            new UpdateVehicleCommand(
                request.registrationNumber(),
                request.vin(),
                request.brand(),
                request.model(),
                request.manufacturingYear(),
                request.type(),
                request.fuelType(),
                request.transmissionType(),
                request.color()));

    return ResponseEntity.ok(
        ApiResponse.success("Vehicle updated successfully", VehicleResponse.from(vehicle)));
  }

  /**
   * Changes the lifecycle status of a vehicle.
   *
   * @param id vehicle identifier
   * @param request requested status
   * @return the updated vehicle
   */
  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<VehicleResponse>> updateStatus(
      @PathVariable UUID id, @Valid @RequestBody UpdateVehicleStatusRequest request) {
    Vehicle vehicle = vehicleService.updateStatus(VehicleId.of(id), request.status());

    return ResponseEntity.ok(
        ApiResponse.success("Vehicle status updated successfully", VehicleResponse.from(vehicle)));
  }

  /**
   * Updates the mileage of a vehicle.
   *
   * @param id vehicle identifier
   * @param request new mileage
   * @return the updated vehicle
   */
  @PatchMapping("/{id}/mileage")
  public ResponseEntity<ApiResponse<VehicleResponse>> updateMileage(
      @PathVariable UUID id, @Valid @RequestBody UpdateVehicleMileageRequest request) {
    Vehicle vehicle = vehicleService.updateMileage(VehicleId.of(id), request.mileage());

    return ResponseEntity.ok(
        ApiResponse.success("Vehicle mileage updated successfully", VehicleResponse.from(vehicle)));
  }

  /**
   * Deletes a vehicle.
   *
   * @param id vehicle identifier
   * @return HTTP 204 when the vehicle was deleted
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    vehicleService.delete(VehicleId.of(id));

    return ResponseEntity.noContent().build();
  }
}
