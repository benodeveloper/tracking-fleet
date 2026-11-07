package com.tracking.fleet.vehicle.api;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tracking.fleet.shared.api.exception.GlobalExceptionHandler;
import com.tracking.fleet.vehicle.application.VehiclePage;
import com.tracking.fleet.vehicle.application.VehicleService;
import com.tracking.fleet.vehicle.application.exception.VehicleAlreadyExistsException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
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

@WebMvcTest(VehicleController.class)
@Import(GlobalExceptionHandler.class)
class VehicleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private VehicleService vehicleService;

  private Vehicle sampleVehicle(UUID id) {
    return Vehicle.restore(
        VehicleId.of(id),
        new RegistrationNumber("ABC-1234"),
        "1HGCM82633A123456",
        new VehicleSpecification("Toyota", "Corolla", 2020, VehicleType.CAR, FuelType.PETROL, TransmissionType.AUTOMATIC),
        "White",
        VehicleStatus.ACTIVE,
        BigDecimal.ZERO,
        Instant.parse("2024-01-01T00:00:00Z"),
        Instant.parse("2024-01-01T00:00:00Z"));
  }

  @Test
  void createReturns201() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.create(any())).thenReturn(sampleVehicle(id));

    String body =
        """
        {
          "registrationNumber": "ABC-1234",
          "vin": "1HGCM82633A123456",
          "brand": "Toyota",
          "model": "Corolla",
          "manufacturingYear": 2020,
          "type": "CAR",
          "fuelType": "PETROL",
          "transmissionType": "AUTOMATIC",
          "color": "White"
        }
        """;

    mockMvc
        .perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.registrationNumber").value("ABC-1234"));
  }

  @Test
  void createValidationFails() throws Exception {
    String body =
        """
        {
          "registrationNumber": "",
          "brand": "",
          "model": "",
          "manufacturingYear": 1800,
          "type": null,
          "fuelType": null,
          "transmissionType": null
        }
        """;

    mockMvc
        .perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void createReturns409OnDuplicate() throws Exception {
    when(vehicleService.create(any())).thenThrow(new VehicleAlreadyExistsException("registration number", "ABC-1234"));

    String body =
        """
        {
          "registrationNumber": "ABC-1234",
          "vin": "1HGCM82633A123456",
          "brand": "Toyota",
          "model": "Corolla",
          "manufacturingYear": 2020,
          "type": "CAR",
          "fuelType": "PETROL",
          "transmissionType": "AUTOMATIC",
          "color": "White"
        }
        """;

    mockMvc
        .perform(post("/api/v1/vehicles").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void getByIdReturns200() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.get(eq(VehicleId.of(id)))).thenReturn(sampleVehicle(id));

    mockMvc
        .perform(get("/api/v1/vehicles/{id}", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.id").value(id.toString()));
  }

  @Test
  void getByIdReturns404() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.get(eq(VehicleId.of(id)))).thenThrow(new VehicleNotFoundException(id));

    mockMvc
        .perform(get("/api/v1/vehicles/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  void getAllReturns200WithPage() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.findAll(0, 20)).thenReturn(new VehiclePage(List.of(sampleVehicle(id)), 0, 20, 1));

    mockMvc
        .perform(get("/api/v1/vehicles"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content", hasSize(1)))
        .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @Test
  void updateReturns200() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.update(eq(VehicleId.of(id)), any())).thenReturn(sampleVehicle(id));

    String body =
        """
        {
          "registrationNumber": "ABC-1234",
          "vin": "1HGCM82633A123456",
          "brand": "Toyota",
          "model": "Corolla",
          "manufacturingYear": 2021,
          "type": "CAR",
          "fuelType": "PETROL",
          "transmissionType": "AUTOMATIC",
          "color": "Red"
        }
        """;

    mockMvc
        .perform(put("/api/v1/vehicles/{id}", id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void updateStatusReturns200() throws Exception {
    UUID id = UUID.randomUUID();
    Vehicle vehicle = sampleVehicle(id);
    when(vehicleService.updateStatus(eq(VehicleId.of(id)), eq(VehicleStatus.UNDER_MAINTENANCE)))
        .thenReturn(vehicle);

    String body = """
        {"status": "UNDER_MAINTENANCE"}
        """;

    mockMvc
        .perform(patch("/api/v1/vehicles/{id}/status", id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void updateMileageReturns200() throws Exception {
    UUID id = UUID.randomUUID();
    when(vehicleService.updateMileage(eq(VehicleId.of(id)), any(BigDecimal.class))).thenReturn(sampleVehicle(id));

    String body = """
        {"mileage": 15000.50}
        """;

    mockMvc
        .perform(patch("/api/v1/vehicles/{id}/mileage", id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  void deleteReturns204() throws Exception {
    UUID id = UUID.randomUUID();
    doNothing().when(vehicleService).delete(eq(VehicleId.of(id)));

    mockMvc
        .perform(delete("/api/v1/vehicles/{id}", id))
        .andExpect(status().isNoContent());
  }
}
