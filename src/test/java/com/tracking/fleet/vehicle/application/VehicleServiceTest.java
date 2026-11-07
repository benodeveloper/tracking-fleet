package com.tracking.fleet.vehicle.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tracking.fleet.vehicle.application.command.CreateVehicleCommand;
import com.tracking.fleet.vehicle.application.command.UpdateVehicleCommand;
import com.tracking.fleet.vehicle.application.exception.VehicleAlreadyExistsException;
import com.tracking.fleet.vehicle.application.exception.VehicleNotFoundException;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleRepository;
import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleStatus;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

  @Mock
  private VehicleRepository vehicleRepository;

  private VehicleService vehicleService;

  @BeforeEach
  void setUp() {
    vehicleService = new VehicleService(vehicleRepository);
  }

  private CreateVehicleCommand createCommand() {
    return new CreateVehicleCommand(
        "ABC-1234",
        "1HGCM82633A123456",
        "Toyota",
        "Corolla",
        2020,
        VehicleType.CAR,
        FuelType.PETROL,
        TransmissionType.AUTOMATIC,
        "White");
  }

  @Test
  void createPersistsVehicle() {
    when(vehicleRepository.existsByRegistrationNumber(any())).thenReturn(false);
    when(vehicleRepository.existsByVin(any())).thenReturn(false);
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Vehicle created = vehicleService.create(createCommand());

    assertThat(created.registrationNumber().value()).isEqualTo("ABC-1234");
    assertThat(created.status()).isEqualTo(VehicleStatus.ACTIVE);
    verify(vehicleRepository).save(any(Vehicle.class));
  }

  @Test
  void createRejectsDuplicateRegistrationNumber() {
    when(vehicleRepository.existsByRegistrationNumber(any())).thenReturn(true);

    assertThatThrownBy(() -> vehicleService.create(createCommand()))
        .isInstanceOf(VehicleAlreadyExistsException.class)
        .hasMessageContaining("registration number");
  }

  @Test
  void getReturnsVehicleOrThrows() {
    UUID id = UUID.randomUUID();
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.of(id),
            new RegistrationNumber("DEF-5678"),
            null,
            new VehicleSpecification("Honda", "Civic", 2019, VehicleType.CAR, FuelType.PETROL, TransmissionType.MANUAL),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.ZERO,
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findById(VehicleId.of(id))).thenReturn(Optional.of(vehicle));

    assertThat(vehicleService.get(VehicleId.of(id)).id().value()).isEqualTo(id);

    UUID missingId = UUID.randomUUID();
    when(vehicleRepository.findById(VehicleId.of(missingId))).thenReturn(Optional.empty());
    assertThatThrownBy(() -> vehicleService.get(VehicleId.of(missingId)))
        .isInstanceOf(VehicleNotFoundException.class);
  }

  @Test
  void findAllReturnsPage() {
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.generate(),
            new RegistrationNumber("GHI-9012"),
            null,
            new VehicleSpecification("Ford", "Transit", 2021, VehicleType.VAN, FuelType.DIESEL, TransmissionType.MANUAL),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.ZERO,
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findAll(0, 20)).thenReturn(List.of(vehicle));
    when(vehicleRepository.count()).thenReturn(1L);

    VehiclePage page = vehicleService.findAll(0, 20);

    assertThat(page.vehicles()).hasSize(1);
    assertThat(page.totalElements()).isEqualTo(1);
  }

  @Test
  void updateModifiesVehicle() {
    UUID id = UUID.randomUUID();
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.of(id),
            new RegistrationNumber("JKL-3456"),
            null,
            new VehicleSpecification("Nissan", "Leaf", 2018, VehicleType.CAR, FuelType.ELECTRIC, TransmissionType.AUTOMATIC),
            "Blue",
            VehicleStatus.ACTIVE,
            BigDecimal.valueOf(10000),
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findById(VehicleId.of(id))).thenReturn(Optional.of(vehicle));
    when(vehicleRepository.existsByVin(any())).thenReturn(false);
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UpdateVehicleCommand command =
        new UpdateVehicleCommand(
            "JKL-3456",
            "1HGCM82633A123456",
            "Nissan",
            "Leaf",
            2019,
            VehicleType.CAR,
            FuelType.ELECTRIC,
            TransmissionType.AUTOMATIC,
            "Red");

    Vehicle updated = vehicleService.update(VehicleId.of(id), command);

    assertThat(updated.color()).isEqualTo("Red");
    ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
    verify(vehicleRepository).save(captor.capture());
    assertThat(captor.getValue().specification().manufacturingYear()).isEqualTo(2019);
  }

  @Test
  void updateStatusChangesStatus() {
    UUID id = UUID.randomUUID();
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.of(id),
            new RegistrationNumber("MNO-7890"),
            null,
            new VehicleSpecification("Volvo", "FH", 2022, VehicleType.TRUCK, FuelType.DIESEL, TransmissionType.AUTOMATIC),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.ZERO,
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findById(VehicleId.of(id))).thenReturn(Optional.of(vehicle));
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Vehicle updated = vehicleService.updateStatus(VehicleId.of(id), VehicleStatus.UNDER_MAINTENANCE);

    assertThat(updated.status()).isEqualTo(VehicleStatus.UNDER_MAINTENANCE);
  }

  @Test
  void updateMileageChangesMileage() {
    UUID id = UUID.randomUUID();
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.of(id),
            new RegistrationNumber("PQR-1111"),
            null,
            new VehicleSpecification("Scania", "R450", 2020, VehicleType.TRUCK, FuelType.DIESEL, TransmissionType.AUTOMATIC),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.valueOf(50000),
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findById(VehicleId.of(id))).thenReturn(Optional.of(vehicle));
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Vehicle updated = vehicleService.updateMileage(VehicleId.of(id), BigDecimal.valueOf(55000));

    assertThat(updated.mileage()).isEqualByComparingTo(BigDecimal.valueOf(55000));
  }

  @Test
  void deleteMarksVehicleAsDeleted() {
    UUID id = UUID.randomUUID();
    Vehicle vehicle =
        Vehicle.restore(
            VehicleId.of(id),
            new RegistrationNumber("STU-2222"),
            null,
            new VehicleSpecification("Isuzu", "D-Max", 2021, VehicleType.TRUCK, FuelType.DIESEL, TransmissionType.MANUAL),
            null,
            VehicleStatus.ACTIVE,
            BigDecimal.ZERO,
            Instant.now(),
            Instant.now());

    when(vehicleRepository.findById(VehicleId.of(id))).thenReturn(Optional.of(vehicle));
    when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(invocation -> invocation.getArgument(0));

    vehicleService.delete(VehicleId.of(id));

    ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
    verify(vehicleRepository).save(captor.capture());
    assertThat(captor.getValue().isDeleted()).isTrue();
    assertThat(captor.getValue().deletedAt()).isNotNull();
  }
}
