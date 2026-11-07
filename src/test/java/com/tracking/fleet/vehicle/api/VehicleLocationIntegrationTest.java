package com.tracking.fleet.vehicle.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class VehicleLocationIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  private String createVehicle(String registrationNumber) throws Exception {
    String response =
        mockMvc
            .perform(
                post("/api/v1/vehicles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "registrationNumber": "%s",
                          "brand": "Toyota",
                          "model": "Corolla",
                          "manufacturingYear": 2020,
                          "type": "CAR",
                          "fuelType": "PETROL",
                          "transmissionType": "AUTOMATIC",
                          "color": "White"
                        }
                        """
                            .formatted(registrationNumber)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    return JsonPath.read(response, "$.data.id");
  }

  private String locationBody(Instant recordedAt, double latitude, double longitude) {
    return """
        {
          "latitude": %s,
          "longitude": %s,
          "speedKph": 42.5,
          "heading": 90,
          "accuracyMeters": 4.0,
          "recordedAt": "%s"
        }
        """
        .formatted(latitude, longitude, recordedAt);
  }

  @Test
  void fullLocationTrackingLifecycle() throws Exception {
    String vehicleId = createVehicle("LOC-1001");
    Instant firstRecordedAt = Instant.now().minusSeconds(60);
    Instant secondRecordedAt = Instant.now().minusSeconds(5);

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(firstRecordedAt, 52.3676, 4.9041)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.vehicleId").value(vehicleId));

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(secondRecordedAt, 52.3700, 4.8950)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations/latest", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.latitude").value(52.37))
        .andExpect(jsonPath("$.data.longitude").value(4.895));

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(2)))
        .andExpect(jsonPath("$.data.content[0].recordedAt").value(secondRecordedAt.toString()))
        .andExpect(jsonPath("$.data.totalElements").value(2));

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .param("from", firstRecordedAt.minusSeconds(1).toString())
                .param("to", firstRecordedAt.plusSeconds(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)));

    mockMvc
        .perform(get("/api/v1/vehicles/live"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].vehicleId").value(vehicleId));
  }

  @Test
  void deletingVehicleRetainsLocationHistoryButExcludesVehicle() throws Exception {
    String vehicleId = createVehicle("LOC-1004");

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(Instant.now().minusSeconds(5), 52.3676, 4.9041)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(delete("/api/v1/vehicles/{vehicleId}", vehicleId))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}", vehicleId))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations", vehicleId))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(get("/api/v1/vehicles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalElements").value(0));

    mockMvc
        .perform(get("/api/v1/vehicles/live"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(0)));

    Integer storedLocations =
        jdbcTemplate.queryForObject(
            "select count(*) from vehicle_locations where vehicle_id = ?",
            Integer.class,
            UUID.fromString(vehicleId));
    assertThat(storedLocations).isEqualTo(1);

    String replacementVehicleId = createVehicle("LOC-1004");
    assertThat(replacementVehicleId).isNotEqualTo(vehicleId);
  }

  @Test
  void invalidCoordinatesReturnBadRequest() throws Exception {
    String vehicleId = createVehicle("LOC-1002");

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(Instant.now().minusSeconds(5), 91, 181)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void unknownVehicleReturnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", "00000000-0000-0000-0000-000000000000")
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(Instant.now().minusSeconds(5), 52.3676, 4.9041)))
        .andExpect(status().isNotFound());
  }

  @Test
  void vehicleWithoutLocationsReturnsNotFound() throws Exception {
    String vehicleId = createVehicle("LOC-1003");

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations/latest", vehicleId))
        .andExpect(status().isNotFound());
  }
}
