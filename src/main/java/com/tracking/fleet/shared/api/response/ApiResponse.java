package com.tracking.fleet.shared.api.response;

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
public record ApiResponse<T>(boolean success, String message, T data, Instant timestamp) {

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
