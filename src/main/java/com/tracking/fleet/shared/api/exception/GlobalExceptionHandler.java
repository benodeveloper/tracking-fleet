package com.tracking.fleet.shared.api.exception;

import com.tracking.fleet.shared.api.response.ApiErrorResponse;
import com.tracking.fleet.vehicle.application.exception.VehicleAlreadyExistsException;
import com.tracking.fleet.vehicle.application.exception.VehicleLocationNotFoundException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Translates application, validation and persistence exceptions into consistent API error
 * responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /**
   * Creates the exception handler advice.
   */
  public GlobalExceptionHandler() {}

  /**
   * Handles missing vehicles and locations.
   *
   * @param exception missing vehicle exception
   * @return HTTP 404 response
   */
  @ExceptionHandler({VehicleNotFoundException.class, VehicleLocationNotFoundException.class})
  public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(ApiErrorResponse.of(exception.getMessage()));
  }

  /**
   * Handles duplicate vehicle conflicts.
   *
   * @param exception duplicate vehicle exception
   * @return HTTP 409 response
   */
  @ExceptionHandler(VehicleAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleAlreadyExists(
      VehicleAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiErrorResponse.of(exception.getMessage()));
  }

  /**
   * Handles request-body bean validation failures.
   *
   * @param exception validation exception
   * @return HTTP 400 response with field errors
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return ResponseEntity.badRequest().body(ApiErrorResponse.of("Validation failed", errors));
  }

  /**
   * Handles constraint violations raised outside request-body binding.
   *
   * @param exception constraint violation exception
   * @return HTTP 400 response with violation details
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
      ConstraintViolationException exception) {
    Map<String, String> errors = new LinkedHashMap<>();

    exception.getConstraintViolations().forEach(
        violation -> errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

    return ResponseEntity.badRequest().body(ApiErrorResponse.of("Validation failed", errors));
  }

  /**
   * Handles authentication failures.
   */
  @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleBadCredentials(
      org.springframework.security.authentication.BadCredentialsException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiErrorResponse.of(exception.getMessage()));
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDenied(
      org.springframework.security.access.AccessDeniedException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiErrorResponse.of("Access denied"));
  }

  /**
   * Handles invalid arguments, path variables and unreadable request bodies.
   *
   * @param exception invalid request exception
   * @return HTTP 400 response
   */
  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
    String message =
        exception instanceof MethodArgumentTypeMismatchException
            ? "Invalid request parameter: " + exception.getMessage()
            : exception.getMessage();

    return ResponseEntity.badRequest().body(ApiErrorResponse.of(message));
  }

  /**
   * Handles invalid domain state transitions.
   *
   * @param exception illegal state exception
   * @return HTTP 409 response
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiErrorResponse.of(exception.getMessage()));
  }

  /**
   * Handles database constraint violations.
   *
   * @param exception data integrity exception
   * @return HTTP 409 response
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(
      DataIntegrityViolationException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(ApiErrorResponse.of("The request conflicts with an existing vehicle"));
  }

  /**
   * Handles unexpected exceptions while avoiding leaking implementation details to clients.
   *
   * @param exception unexpected exception
   * @return HTTP 500 response
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
    log.error("Unexpected error while handling request", exception);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiErrorResponse.of("An unexpected error occurred"));
  }
}
