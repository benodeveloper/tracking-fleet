package com.tracking.fleet.vehicle.domain.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Validated and normalized vehicle registration number.
 *
 * <p>Registration numbers are trimmed and converted to upper case. Only letters, digits and dashes
 * are accepted, with a length between 2 and 30 characters.
 *
 * @param value normalized registration number
 */
public record RegistrationNumber(String value) {

  private static final Pattern VALID_FORMAT = Pattern.compile("^[A-Z0-9-]{2,30}$");

  /**
   * Validates and normalizes a registration number.
   *
   * @param value raw registration number, must not be {@code null}
   * @throws NullPointerException if {@code value} is {@code null}
   * @throws IllegalArgumentException if the normalized value does not match the expected format
   */
  public RegistrationNumber {
    Objects.requireNonNull(value, "Registration number cannot be null");

    value = value.trim().toUpperCase(Locale.ROOT);

    if (!VALID_FORMAT.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid vehicle registration number");
    }
  }
}
