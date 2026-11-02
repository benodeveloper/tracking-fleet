package com.tracking.fleet.vehicle.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class VehicleTest {

  private static final RegistrationNumber REGISTRATION_NUMBER =
      new RegistrationNumber("ABC-1234");
  private static final VehicleSpecification SPECIFICATION =
      new VehicleSpecification("Toyota", "Corolla", 2020, VehicleType.CAR, FuelType.PETROL, TransmissionType.AUTOMATIC);

  @Test
  void registerCreatesActiveVehicleWithZeroMileage() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, "1HGCM82633A123456", SPECIFICATION, "White");

    assertThat(vehicle.status()).isEqualTo(VehicleStatus.ACTIVE);
    assertThat(vehicle.mileage()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(vehicle.vin()).isEqualTo("1HGCM82633A123456");
    assertThat(vehicle.color()).isEqualTo("White");
  }

  @Test
  void updateMileageRejectsLowerMileage() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, null, SPECIFICATION, null);
    vehicle.updateMileage(BigDecimal.valueOf(1000));

    assertThatThrownBy(() -> vehicle.updateMileage(BigDecimal.valueOf(500)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("lower than current mileage");
  }

  @Test
  void deactivateFromActive() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, null, SPECIFICATION, null);
    vehicle.deactivate();

    assertThat(vehicle.status()).isEqualTo(VehicleStatus.INACTIVE);
  }

  @Test
  void sendToMaintenanceAndComplete() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, null, SPECIFICATION, null);
    vehicle.sendToMaintenance();
    assertThat(vehicle.status()).isEqualTo(VehicleStatus.UNDER_MAINTENANCE);

    vehicle.completeMaintenance();
    assertThat(vehicle.status()).isEqualTo(VehicleStatus.ACTIVE);
  }

  @Test
  void retiredCannotBeActivated() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, null, SPECIFICATION, null);
    vehicle.retire();

    assertThatThrownBy(vehicle::activate)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("retired vehicle cannot be activated");
  }

  @Test
  void transitionToSupportsValidTransitions() {
    Vehicle vehicle = Vehicle.register(REGISTRATION_NUMBER, null, SPECIFICATION, null);
    vehicle.transitionTo(VehicleStatus.UNDER_MAINTENANCE);
    vehicle.transitionTo(VehicleStatus.ACTIVE);
    vehicle.transitionTo(VehicleStatus.INACTIVE);
    vehicle.transitionTo(VehicleStatus.RETIRED);

    assertThat(vehicle.status()).isEqualTo(VehicleStatus.RETIRED);
  }
}
