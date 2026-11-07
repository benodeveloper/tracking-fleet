package com.tracking.fleet.shared.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI metadata for the tracking fleet REST API.
 *
 * <p>The generated specification is available at {@code /v3/api-docs} and is rendered by Swagger UI
 * at {@code /swagger-ui.html}.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Creates the OpenAPI configuration.
   */
  public OpenApiConfig() {}

  /**
   * Builds the top-level OpenAPI document metadata.
   *
   * @return OpenAPI configuration
   */
  @Bean
  public OpenAPI fleetOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Tracking Fleet API")
                .version("v1")
                .description(
                    """
                    REST API for managing a vehicle fleet and recording live vehicle positions.

                    All successful and error responses are wrapped in a consistent response envelope.
                    Location history can be filtered by recorded time and is paginated.
                    """)
                .contact(new Contact().name("Tracking Fleet"))
                .license(new License().name("Apache License 2.0")))
        .servers(List.of(new Server().url("/").description("Current server")))
        .tags(
            List.of(
                new Tag().name("Vehicles").description("Create and manage fleet vehicles"),
                new Tag()
                    .name("Locations")
                    .description("Record and query vehicle GPS positions")));
  }
}
