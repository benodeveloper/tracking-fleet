package com.tracking.fleet.vehicle.api;

import com.tracking.fleet.shared.api.response.ApiResponse;
import com.tracking.fleet.shared.api.response.PageResponse;
import com.tracking.fleet.vehicle.api.dto.RecordVehicleLocationRequest;
import com.tracking.fleet.vehicle.api.dto.VehicleLocationResponse;
import com.tracking.fleet.vehicle.application.VehicleLocationPage;
import com.tracking.fleet.vehicle.application.VehicleLocationService;
import com.tracking.fleet.vehicle.application.command.RecordVehicleLocationCommand;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for vehicle location ingestion and queries.
 */
@RequestMapping("/api/v1/vehicles")
@RestController
public class VehicleLocationController {

  private final VehicleLocationService vehicleLocationService;

  /**
   * Creates the vehicle location controller.
   *
   * @param vehicleLocationService location application service
   */
  public VehicleLocationController(VehicleLocationService vehicleLocationService) {
    this.vehicleLocationService = vehicleLocationService;
  }

  /**
   * Records a location for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @param request location data
   * @return the recorded location with HTTP 201
   */
  @PostMapping("/{vehicleId}/locations")
  public ResponseEntity<ApiResponse<VehicleLocationResponse>> record(
      @PathVariable UUID vehicleId, @Valid @RequestBody RecordVehicleLocationRequest request) {
    VehicleLocation location =
        vehicleLocationService.record(
            VehicleId.of(vehicleId),
            new RecordVehicleLocationCommand(
                request.latitude(),
                request.longitude(),
                request.speedKph(),
                request.heading(),
                request.accuracyMeters(),
                request.recordedAt()));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Location recorded successfully", VehicleLocationResponse.from(location)));
  }

  /**
   * Returns the latest location for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @return the latest location
   */
  @GetMapping("/{vehicleId}/locations/latest")
  public ResponseEntity<ApiResponse<VehicleLocationResponse>> getLatest(
      @PathVariable UUID vehicleId) {
    VehicleLocation location = vehicleLocationService.getLatest(VehicleId.of(vehicleId));

    return ResponseEntity.ok(
        ApiResponse.success(
            "Latest location retrieved successfully", VehicleLocationResponse.from(location)));
  }

  /**
   * Returns a page of location history for a vehicle.
   *
   * @param vehicleId vehicle identifier
   * @param from optional inclusive lower bound for the recorded timestamp
   * @param to optional inclusive upper bound for the recorded timestamp
   * @param page zero-based page index
   * @param size page size
   * @return the requested history page
   */
  @GetMapping("/{vehicleId}/locations")
  public ResponseEntity<ApiResponse<PageResponse<VehicleLocationResponse>>> getHistory(
      @PathVariable UUID vehicleId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    VehicleLocationPage history =
        vehicleLocationService.findHistory(VehicleId.of(vehicleId), from, to, page, size);

    var content = history.locations().stream().map(VehicleLocationResponse::from).toList();
    var pageResponse = PageResponse.of(content, history.page(), history.size(), history.totalElements());

    return ResponseEntity.ok(
        ApiResponse.success("Location history retrieved successfully", pageResponse));
  }

  /**
   * Returns the latest reported location for every vehicle.
   *
   * @return latest locations, newest first
   */
  @GetMapping("/live")
  public ResponseEntity<ApiResponse<List<VehicleLocationResponse>>> getLive() {
    List<VehicleLocationResponse> locations =
        vehicleLocationService.findLive().stream().map(VehicleLocationResponse::from).toList();

    return ResponseEntity.ok(
        ApiResponse.success("Live vehicle locations retrieved successfully", locations));
  }
}
