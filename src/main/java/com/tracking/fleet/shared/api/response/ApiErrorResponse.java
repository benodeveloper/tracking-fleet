package com.tracking.fleet.shared.api.response;

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
public record ApiErrorResponse(
    boolean success, String message, Map<String, String> errors, Instant timestamp) {

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
