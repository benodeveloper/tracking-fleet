package com.tracking.fleet.vehicle.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tracking.fleet.vehicle.application.command.RecordVehicleLocationCommand;
import com.tracking.fleet.vehicle.application.exception.VehicleLocationNotFoundException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationHistory;
import com.tracking.fleet.vehicle.domain.VehicleLocationRepository;
import com.tracking.fleet.vehicle.domain.VehicleRepository;
import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import com.tracking.fleet.vehicle.infrastructure.websocket.VehicleLocationBroadcaster;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleLocationServiceTest {

  @Mock
  private VehicleRepository vehicleRepository;

  @Mock
  private VehicleLocationRepository vehicleLocationRepository;

  @Mock
  private VehicleLocationBroadcaster broadcaster;

  private VehicleLocationService vehicleLocationService;

  private VehicleId vehicleId;
  private Vehicle vehicle;

  @BeforeEach
  void setUp() {
    vehicleLocationService =
        new VehicleLocationService(vehicleRepository, vehicleLocationRepository, broadcaster);

    vehicleId = VehicleId.generate();
    vehicle =
        Vehicle.restore(
            vehicleId,
            new RegistrationNumber("LOC-0001"),
            null,
            new VehicleSpecification(
                "Toyota", "Corolla", 2020, VehicleType.CAR, FuelType.PETROL, TransmissionType.AUTOMATIC),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.ZERO,
            Instant.now(),
            Instant.now());
  }

  private RecordVehicleLocationCommand command() {
    return new RecordVehicleLocationCommand(
        BigDecimal.valueOf(52.3676),
        BigDecimal.valueOf(4.9041),
        BigDecimal.valueOf(42.5),
        BigDecimal.valueOf(90),
        BigDecimal.valueOf(4.0),
        Instant.now().minusSeconds(5));
  }

  private VehicleLocation location() {
    return VehicleLocation.record(
        vehicleId,
        BigDecimal.valueOf(52.3676),
        BigDecimal.valueOf(4.9041),
        BigDecimal.valueOf(42.5),
        BigDecimal.valueOf(90),
        BigDecimal.valueOf(4.0),
        Instant.now().minusSeconds(5));
  }

  @Test
  void recordSavesLocationForExistingVehicle() {
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    when(vehicleLocationRepository.save(any(VehicleLocation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    VehicleLocation recorded = vehicleLocationService.record(vehicleId, command());

    assertThat(recorded.vehicleId()).isEqualTo(vehicleId);
    assertThat(recorded.latitude()).isEqualByComparingTo("52.3676");
    verify(vehicleLocationRepository).save(any(VehicleLocation.class));
  }

  @Test
  void recordRejectsUnknownVehicle() {
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleLocationService.record(vehicleId, command()))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void getLatestReturnsLocation() {
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    when(vehicleLocationRepository.findLatestByVehicleId(vehicleId))
        .thenReturn(Optional.of(location()));

    assertThat(vehicleLocationService.getLatest(vehicleId).vehicleId()).isEqualTo(vehicleId);
  }

  @Test
  void getLatestRejectsVehicleWithoutLocations() {
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    when(vehicleLocationRepository.findLatestByVehicleId(vehicleId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> vehicleLocationService.getLatest(vehicleId))
        .isInstanceOf(VehicleLocationNotFoundException.class);
  }

  @Test
  void findHistoryReturnsPage() {
    Instant from = Instant.now().minusSeconds(3600);
    Instant to = Instant.now();

    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
    when(vehicleLocationRepository.findHistory(vehicleId, from, to, 0, 10))
        .thenReturn(new VehicleLocationHistory(List.of(location()), 1));

    VehicleLocationPage page = vehicleLocationService.findHistory(vehicleId, from, to, 0, 10);

    assertThat(page.locations()).hasSize(1);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void findHistoryRejectsInvalidTimeRange() {
    Instant from = Instant.now();
    Instant to = from.minusSeconds(60);

    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

    assertThatThrownBy(() -> vehicleLocationService.findHistory(vehicleId, from, to, 0, 10))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("'from'");
  }

  @Test
  void findHistoryRejectsInvalidPagination() {
    when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));

    assertThatThrownBy(() -> vehicleLocationService.findHistory(vehicleId, null, null, -1, 10))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> vehicleLocationService.findHistory(vehicleId, null, null, 0, 101))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void findLiveReturnsLatestLocations() {
    when(vehicleLocationRepository.findLatestForAllVehicles()).thenReturn(List.of(location()));

    assertThat(vehicleLocationService.findLive()).hasSize(1);
  }
}
