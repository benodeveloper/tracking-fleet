package com.tracking.fleet.driver.api;

import com.tracking.fleet.auth.domain.UserAccount;
import com.tracking.fleet.auth.domain.UserAccountRepository;
import com.tracking.fleet.driver.api.dto.DriverVehicleResponse;
import com.tracking.fleet.shared.api.response.ApiResponse;
import com.tracking.fleet.vehicle.api.dto.RecordVehicleLocationRequest;
import com.tracking.fleet.vehicle.api.dto.VehicleLocationResponse;
import com.tracking.fleet.vehicle.application.VehicleLocationService;
import com.tracking.fleet.vehicle.application.VehicleService;
import com.tracking.fleet.vehicle.application.command.RecordVehicleLocationCommand;
import com.tracking.fleet.vehicle.domain.VehicleId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/driver")
@Tag(name = "Driver", description = "Driver mobile app operations")
public class DriverController {

  private final UserAccountRepository userAccountRepository;
  private final VehicleService vehicleService;
  private final VehicleLocationService vehicleLocationService;

  public DriverController(
      UserAccountRepository userAccountRepository,
      VehicleService vehicleService,
      VehicleLocationService vehicleLocationService) {
    this.userAccountRepository = userAccountRepository;
    this.vehicleService = vehicleService;
    this.vehicleLocationService = vehicleLocationService;
  }

  @GetMapping("/me/vehicle")
  @Operation(summary = "Get the vehicle assigned to the authenticated driver")
  public ResponseEntity<ApiResponse<DriverVehicleResponse>> myVehicle(Principal principal) {
    UserAccount user = requireDriver(principal);
    if (user.assignedVehicleId() == null) {
      throw new IllegalStateException("No vehicle assigned to this driver");
    }
    var vehicle = vehicleService.get(VehicleId.of(user.assignedVehicleId()));
    return ResponseEntity.ok(ApiResponse.success("Assigned vehicle", DriverVehicleResponse.from(vehicle)));
  }

  @PostMapping("/me/location")
  @Operation(summary = "Report the current GPS position for the driver's assigned vehicle")
  public ResponseEntity<ApiResponse<VehicleLocationResponse>> reportMyLocation(
      Principal principal, @Valid @RequestBody RecordVehicleLocationRequest request) {
    UserAccount user = requireDriver(principal);
    if (user.assignedVehicleId() == null) {
      throw new IllegalStateException("No vehicle assigned to this driver");
    }
    var location = vehicleLocationService.record(VehicleId.of(user.assignedVehicleId()),
        new RecordVehicleLocationCommand(
            request.latitude(), request.longitude(), request.speedKph(), request.heading(),
            request.accuracyMeters(), request.recordedAt()));
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Location recorded", VehicleLocationResponse.from(location)));
  }

  private UserAccount requireDriver(Principal principal) {
    UserAccount user = userAccountRepository.findByUsername(principal.getName())
        .orElseThrow(() -> new AccessDeniedException("Unknown user"));
    if (!"DRIVER".equals(user.role())) {
      throw new AccessDeniedException("Driver role required");
    }
    return user;
  }
}
