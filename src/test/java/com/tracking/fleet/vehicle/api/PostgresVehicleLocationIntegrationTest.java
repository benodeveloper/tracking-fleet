package com.tracking.fleet.vehicle.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostgresVehicleLocationIntegrationTest {

  @Container
  private static final PostgreSQLContainer POSTGRES =
      new PostgreSQLContainer("postgres:16-alpine")
          .withDatabaseName("fleet_test")
          .withUsername("fleet")
          .withPassword("fleet");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.flyway.enabled", () -> true);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
  }

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private EntityManager entityManager;

  @Test
  void flywaySchemaSupportsLocationQueries() throws Exception {
    assertThat(
            jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'vehicle_locations'",
                Integer.class))
        .isEqualTo(1);

    String vehicleId = createVehicle("PG-1001");
    Instant recordedAt = Instant.now().minusSeconds(5);

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(recordedAt, 52.3676, 4.9041)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations/latest", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.latitude").value(52.3676))
        .andExpect(jsonPath("$.data.longitude").value(4.9041));

    mockMvc
        .perform(get("/api/v1/vehicles/{vehicleId}/locations", vehicleId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)))
        .andExpect(jsonPath("$.data.totalElements").value(1));

    mockMvc
        .perform(get("/api/v1/vehicles/live"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data", hasSize(1)))
        .andExpect(jsonPath("$.data[0].vehicleId").value(vehicleId));
  }

  @Test
  void historyFiltersWorkInPostgres() throws Exception {
    String vehicleId = createVehicle("PG-1003");
    Instant firstRecordedAt = Instant.now().minusSeconds(120);
    Instant secondRecordedAt = Instant.now().minusSeconds(5);

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(firstRecordedAt, 52.3676, 4.9041)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(secondRecordedAt, 52.3700, 4.8950)))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .param("from", firstRecordedAt.minusSeconds(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(2)));

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .param("to", firstRecordedAt.plusSeconds(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)));

    mockMvc
        .perform(
            get("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .param("from", firstRecordedAt.minusSeconds(1).toString())
                .param("to", firstRecordedAt.plusSeconds(1).toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content", hasSize(1)));
  }

  @Test
  void deletingVehicleCascadesLocationRowsInPostgres() throws Exception {
    String vehicleId = createVehicle("PG-1002");

    mockMvc
        .perform(
            post("/api/v1/vehicles/{vehicleId}/locations", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(locationBody(Instant.now().minusSeconds(5), 48.8566, 2.3522)))
        .andExpect(status().isCreated());

    Integer countBefore =
        jdbcTemplate.queryForObject(
            "select count(*) from vehicle_locations where vehicle_id = ?",
            Integer.class,
            UUID.fromString(vehicleId));
    assertThat(countBefore).isEqualTo(1);

    mockMvc
        .perform(delete("/api/v1/vehicles/{vehicleId}", vehicleId))
        .andExpect(status().isNoContent());

    entityManager.flush();

    Integer countAfter =
        jdbcTemplate.queryForObject(
            "select count(*) from vehicle_locations where vehicle_id = ?",
            Integer.class,
            UUID.fromString(vehicleId));
    assertThat(countAfter).isZero();
  }

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
}
