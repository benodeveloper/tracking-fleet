package com.tracking.fleet.vehicle.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class VehicleLocationTest {

  private static final VehicleId VEHICLE_ID = VehicleId.generate();

  private static VehicleLocation location(BigDecimal latitude, BigDecimal longitude) {
    return VehicleLocation.record(
        VEHICLE_ID,
        latitude,
        longitude,
        BigDecimal.valueOf(50),
        BigDecimal.valueOf(90),
        BigDecimal.valueOf(5),
        Instant.now().minusSeconds(5));
  }

  @Test
  void recordCreatesLocationWithGeneratedIdAndReceivedAt() {
    Instant recordedAt = Instant.now().minusSeconds(10);

    VehicleLocation location =
        VehicleLocation.record(
            VEHICLE_ID,
            BigDecimal.valueOf(52.3676),
            BigDecimal.valueOf(4.9041),
            BigDecimal.valueOf(42.5),
            BigDecimal.valueOf(180),
            BigDecimal.valueOf(3.5),
            recordedAt);

    assertThat(location.id()).isNotNull();
    assertThat(location.vehicleId()).isEqualTo(VEHICLE_ID);
    assertThat(location.latitude()).isEqualByComparingTo("52.3676");
    assertThat(location.longitude()).isEqualByComparingTo("4.9041");
    assertThat(location.recordedAt()).isEqualTo(recordedAt);
    assertThat(location.receivedAt()).isNotNull();
  }

  @Test
  void latitudeMustBeWithinRange() {
    assertThatThrownBy(() -> location(BigDecimal.valueOf(91), BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Latitude");
    assertThatThrownBy(() -> location(BigDecimal.valueOf(-91), BigDecimal.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Latitude");
  }

  @Test
  void longitudeMustBeWithinRange() {
    assertThatThrownBy(() -> location(BigDecimal.ZERO, BigDecimal.valueOf(181)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Longitude");
  }

  @Test
  void speedAndAccuracyCannotBeNegative() {
    assertThatThrownBy(
            () ->
                VehicleLocation.record(
                    VEHICLE_ID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.valueOf(-1),
                    null,
                    null,
                    Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Speed");

    assertThatThrownBy(
            () ->
                VehicleLocation.record(
                    VEHICLE_ID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    null,
                    BigDecimal.valueOf(-1),
                    Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Accuracy");
  }

  @Test
  void headingMustBeLessThan360() {
    assertThatThrownBy(
            () ->
                VehicleLocation.record(
                    VEHICLE_ID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    BigDecimal.valueOf(360),
                    null,
                    Instant.now()))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Heading");
  }

  @Test
  void recordedAtCannotBeInTheFuture() {
    assertThatThrownBy(
            () ->
                VehicleLocation.record(
                    VEHICLE_ID,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    null,
                    null,
                    null,
                    Instant.now().plusSeconds(3600)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("future");
  }
}
