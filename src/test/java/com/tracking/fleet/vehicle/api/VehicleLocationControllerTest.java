package com.tracking.fleet.vehicle.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tracking.fleet.shared.api.exception.GlobalExceptionHandler;
import com.tracking.fleet.vehicle.application.VehicleLocationPage;
import com.tracking.fleet.vehicle.application.VehicleLocationService;
import com.tracking.fleet.vehicle.application.exception.VehicleLocationNotFoundException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationId;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(VehicleLocationController.class)
@Import(GlobalExceptionHandler.class)
class VehicleLocationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private VehicleLocationService vehicleLocationService;

  private VehicleLocation sampleLocation(UUID vehicleId) {
    return VehicleLocation.restore(
        VehicleLocationId.generate(),
        VehicleId.of(vehicleId),
        BigDecimal.valueOf(52.3676),
        BigDecimal.valueOf(4.9041),
        BigDecimal.valueOf(42.5),
        BigDecimal.valueOf(90),
        BigDecimal.valueOf(4.0),
        Instant.parse("2024-01-01T10:00:00Z"),
        Instant.parse("2024-01-01T10:00:01Z"));
  }

  private String locationBody() {
    return """
        {
          "latitude": 52.3676,
          "longitude": 4.9041,
          "speedKph": 42.5,
          "heading": 90,
          "accuracyMeters": 4.0,
          "recordedAt": "2024-01-01T10:00:00Z"
        }
        """;
  }

  @Test
  void recordReturns201() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.record(eq(VehicleId.of(vehicleId)), any()))
        .thenReturn(sampleLocation(vehicleId));

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.vehicleId").value(vehicleId.toString()));
  }

  @Test
  void recordValidationFails() throws Exception {
    String body = """
        {"latitude": 100, "longitude": 200}
        """;

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void recordReturns404ForUnknownVehicle() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.record(eq(VehicleId.of(vehicleId)), any()))
        .thenThrow(new VehicleNotFoundException(vehicleId));

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody()))
        .andExpect(status().isNotFound());
  }

  @Test
  void getLatestReturns200() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.getLatest(VehicleId.of(vehicleId)))
        .thenReturn(sampleLocation(vehicleId));

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations/latest", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.latitude").value(52.3676));
  }

  @Test
  void getLatestReturns404WithoutLocations() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.getLatest(VehicleId.of(vehicleId)))
        .thenThrow(new VehicleLocationNotFoundException(VehicleId.of(vehicleId)));

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations/latest", vehicleId))
        .andExpect(status().isNotFound());
  }

  @Test
  void getHistoryReturns200() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.findHistory(
            eq(VehicleId.of(vehicleId)), isNull(), isNull(), eq(0), eq(20)))
        .thenReturn(new VehicleLocationPage(List.of(sampleLocation(vehicleId)), 0, 20, 1));

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @Test
  void getLiveReturns200() throws Exception {
    UUID vehicleId = UUID.randomUUID();
    when(vehicleLocationService.findLive()).thenReturn(List.of(sampleLocation(vehicleId)));

    mockMvc
        .perform(get("/api/v1/vehicles/live"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)));
  }
}
