package com.tracking.fleet.shared.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.Map;

/**
 * Standard envelope for failed API responses.
 *
 * @param success always {@code false}
 * @param message human-readable error message
 * @param errors field-specific validation errors, when available
 * @param timestamp time at which the response was created
 */
@Schema(name = "ApiErrorResponse", description = "Standard wrapper for failed API responses")
public record ApiErrorResponse(
    @Schema(description = "Always false for failed responses", example = "false") boolean success,
    @Schema(description = "Human-readable error message", example = "Vehicle not found")
        String message,
    @Schema(
            description = "Field-specific validation errors, keyed by field name",
            example = "{\"registrationNumber\": \"Registration number is required\"}")
        Map<String, String> errors,
    @Schema(
            description = "Time at which the response was created",
            example = "2026-01-01T10:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY)
        Instant timestamp) {

  /**
   * Creates an error response and defensively copies the error map.
   */
  public ApiErrorResponse {
    errors = Map.copyOf(errors);
  }

  /**
   * Creates an error response without field-specific details.
   *
   * @param message human-readable error message
   * @return error response
   */
  public static ApiErrorResponse of(String message) {
    return new ApiErrorResponse(false, message, Map.of(), Instant.now());
  }

  /**
   * Creates an error response with field-specific details.
   *
   * @param message human-readable error message
   * @param errors field-specific validation errors
   * @return error response
   */
  public static ApiErrorResponse of(String message, Map<String, String> errors) {
    return new ApiErrorResponse(false, message, errors, Instant.now());
  }
}
