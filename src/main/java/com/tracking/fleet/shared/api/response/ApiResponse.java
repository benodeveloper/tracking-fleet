package com.tracking.fleet.shared.api.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * Standard envelope for successful API responses.
 *
 * @param success whether the request succeeded
 * @param message human-readable result message
 * @param data response payload
 * @param timestamp time at which the response was created
 * @param <T> payload type
 */
@Schema(
    name = "ApiResponse",
    description = "Standard wrapper for successful API responses")
public record ApiResponse<T>(
    @Schema(description = "Always true for successful responses", example = "true")
        boolean success,
    @Schema(description = "Human-readable result message", example = "Vehicle created successfully")
        String message,
    @Schema(description = "Response payload") T data,
    @Schema(
            description = "Time at which the response was created",
            example = "2026-01-01T10:00:00Z",
            accessMode = Schema.AccessMode.READ_ONLY)
        Instant timestamp) {

  /**
   * Creates a successful response envelope.
   *
   * @param message human-readable result message
   * @param data response payload
   * @param <T> payload type
   * @return success response
   */
  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<T>(true, message, data, Instant.now());
  }
}
