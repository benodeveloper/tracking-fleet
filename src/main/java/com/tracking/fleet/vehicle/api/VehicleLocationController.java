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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Locations", description = "Record and query vehicle GPS positions")
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
  @Operation(
      summary = "Record a vehicle location",
      description =
          "Stores a GPS position reported by a vehicle. Recorded time cannot be more than 60 seconds in the future.",
      operationId = "recordVehicleLocation")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "201", description = "Location recorded"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", description = "Invalid location data"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", description = "Vehicle not found")
  })
  public ResponseEntity<ApiResponse<VehicleLocationResponse>> record(
      @Parameter(description = "Vehicle UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID vehicleId,
      @Valid @RequestBody RecordVehicleLocationRequest request) {
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
  @Operation(
      summary = "Get the latest vehicle location",
      description = "Returns the most recently recorded location for a vehicle.",
      operationId = "getLatestVehicleLocation")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", description = "Latest location returned"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", description = "Vehicle or location not found")
  })
  public ResponseEntity<ApiResponse<VehicleLocationResponse>> getLatest(
      @Parameter(description = "Vehicle UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID vehicleId) {
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
  @Operation(
      summary = "Get vehicle location history",
      description =
          "Returns a paginated location history for a vehicle, newest first. Both time filters are optional and inclusive.",
      operationId = "getVehicleLocationHistory")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", description = "Location history returned"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "400", description = "Invalid pagination or time range"),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "404", description = "Vehicle not found")
  })
  public ResponseEntity<ApiResponse<PageResponse<VehicleLocationResponse>>> getHistory(
      @Parameter(description = "Vehicle UUID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
          @PathVariable
          UUID vehicleId,
      @Parameter(
              description = "Inclusive lower bound for recorded time (ISO-8601)",
              example = "2026-01-01T00:00:00Z")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant from,
      @Parameter(
              description = "Inclusive upper bound for recorded time (ISO-8601)",
              example = "2026-01-02T00:00:00Z")
          @RequestParam(required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant to,
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size between 1 and 100", example = "20")
          @RequestParam(defaultValue = "20")
          int size) {
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
  @Operation(
      summary = "Get live vehicle locations",
      description = "Returns the most recent position of every vehicle that has reported a location.",
      operationId = "getLiveVehicleLocations")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200", description = "Latest locations returned")
  })
  public ResponseEntity<ApiResponse<List<VehicleLocationResponse>>> getLive() {
    List<VehicleLocationResponse> locations =
        vehicleLocationService.findLive().stream().map(VehicleLocationResponse::from).toList();

    return ResponseEntity.ok(
        ApiResponse.success("Live vehicle locations retrieved successfully", locations));
  }
}
