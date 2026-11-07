package com.tracking.fleet.shared.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiDocumentationTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void openApiDocumentDescribesTheRestApi() throws Exception {
    mockMvc
        .perform(get("/v3/api-docs"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.info.title").value("Tracking Fleet API"))
        .andExpect(jsonPath("$.paths['/api/v1/vehicles']").exists())
        .andExpect(jsonPath("$.paths['/api/v1/vehicles/{vehicleId}/locations']").exists())
        .andExpect(
            jsonPath("$.components.schemas.VehicleResponse.properties.registrationNumber.example")
                .value("AB-123-CD"));
  }

  @Test
  void swaggerUiIsAvailable() throws Exception {
    mockMvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
  }
}
