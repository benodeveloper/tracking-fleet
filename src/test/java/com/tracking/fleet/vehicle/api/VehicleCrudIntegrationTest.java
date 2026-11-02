package com.tracking.fleet.vehicle.api;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class VehicleCrudIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  private String vehicleBody(String registrationNumber, String brand, int manufacturingYear) {
    return """
        {
          "registrationNumber": "%s",
          "vin": "1HGCM82633A123456",
          "brand": "%s",
          "model": "Corolla",
          "manufacturingYear": %d,
          "type": "CAR",
          "fuelType": "PETROL",
          "transmissionType": "AUTOMATIC",
          "color": "White"
        }
        """
        .formatted(registrationNumber, brand, manufacturingYear);
  }

  @Test
  void fullCrudLifecycle() throws Exception {
    String createResult =
        mockMvc
            .perform(
                post("/api/v1/vehicles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(vehicleBody("INT-0001", "Toyota", 2020)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("ACTIVE"))
            .andExpect(jsonPath("$.data.mileage").value(0))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String id = JsonPath.read(createResult, "$.data.id");

    mockMvc
        .perform(get("/api/v1/vehicles/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.registrationNumber").value("INT-0001"))
        .andExpect(jsonPath("$.data.brand").value("Toyota"));

    mockMvc
        .perform(get("/api/v1/vehicles").param("page", "0").param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)))
        .andExpect(jsonPath("$.data.totalElements").value(1));

    mockMvc
        .perform(
            put("/api/v1/vehicles/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleBody("INT-0001", "Toyota Updated", 2021)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.brand").value("Toyota Updated"))
        .andExpect(jsonPath("$.data.manufacturingYear").value(2021));

    mockMvc
        .perform(
            patch("/api/v1/vehicles/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"status": "UNDER_MAINTENANCE"}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("UNDER_MAINTENANCE"));

    mockMvc
        .perform(
            patch("/api/v1/vehicles/{id}/mileage", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"mileage": 15000.50}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.mileage").value(15000.50));

    mockMvc.perform(delete("/api/v1/vehicles/{id}", id)).andExpect(status().isNoContent());

    mockMvc.perform(get("/api/v1/vehicles/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void duplicateRegistrationNumberReturnsConflict() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleBody("INT-0002", "Toyota", 2020)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleBody("INT-0002", "Honda", 2021)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void unknownVehicleReturnsNotFound() throws Exception {
    mockMvc
        .perform(get("/api/v1/vehicles/{id}", "00000000-0000-0000-0000-000000000000"))
        .andExpect(status().isNotFound());
  }
}
