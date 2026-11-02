package com.tracking.fleet.vehicle.application;

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
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that orchestrates vehicle CRUD operations.
 *
 * <p>The service enforces uniqueness rules for registration numbers and VINs, validates
 * pagination, and translates domain exceptions into application exceptions.
 */
@Service
@Transactional
public class VehicleService {

  private final VehicleRepository vehicleRepository;

  /**
   * Creates the vehicle application service.
   *
   * @param vehicleRepository vehicle repository port
   */
  public VehicleService(VehicleRepository vehicleRepository) {
    this.vehicleRepository = Objects.requireNonNull(vehicleRepository);
  }

  /**
   * Creates and persists a new vehicle.
   *
   * @param command data for the new vehicle
   * @return the created vehicle
   * @throws VehicleAlreadyExistsException if the registration number or VIN is already in use
   */
  public Vehicle create(CreateVehicleCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");

    Vehicle vehicle =
        Vehicle.register(
            new RegistrationNumber(command.registrationNumber()),
            command.vin(),
            specification(
                command.brand(),
                command.model(),
                command.manufacturingYear(),
                command.type(),
                command.fuelType(),
                command.transmissionType()),
            command.color());

    checkRegistrationNumberIsUnique(vehicle);
    checkVinIsUnique(vehicle);

    return save(vehicle);
  }

  /**
   * Returns a vehicle by its identifier.
   *
   * @param id vehicle identifier
   * @return the vehicle
   * @throws VehicleNotFoundException if the vehicle does not exist
   */
  @Transactional(readOnly = true)
  public Vehicle get(VehicleId id) {
    Objects.requireNonNull(id, "Vehicle ID cannot be null");

    return vehicleRepository.findById(id).orElseThrow(() -> new VehicleNotFoundException(id));
  }

  /**
   * Returns a page of vehicles.
   *
   * @param page zero-based page index
   * @param size page size, between 1 and 100
   * @return the requested page
   * @throws IllegalArgumentException if the pagination arguments are invalid
   */
  @Transactional(readOnly = true)
  public VehiclePage findAll(int page, int size) {
    validatePagination(page, size);

    var vehicles = vehicleRepository.findAll(page, size);
    long totalElements = vehicleRepository.count();

    return new VehiclePage(vehicles, page, size, totalElements);
  }

  /**
   * Updates the descriptive data of an existing vehicle.
   *
   * @param id vehicle identifier
   * @param command new vehicle data
   * @return the updated vehicle
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws VehicleAlreadyExistsException if the new registration number or VIN is already in use
   */
  public Vehicle update(VehicleId id, UpdateVehicleCommand command) {
    Objects.requireNonNull(command, "Command cannot be null");

    Vehicle vehicle = get(id);

    RegistrationNumber previousRegistrationNumber = vehicle.registrationNumber();
    String previousVin = vehicle.vin();

    vehicle.updateDetails(
        new RegistrationNumber(command.registrationNumber()),
        command.vin(),
        specification(
            command.brand(),
            command.model(),
            command.manufacturingYear(),
            command.type(),
            command.fuelType(),
            command.transmissionType()),
        command.color());

    if (!vehicle.registrationNumber().equals(previousRegistrationNumber)) {
      checkRegistrationNumberIsUnique(vehicle);
    }

    if (!Objects.equals(vehicle.vin(), previousVin) && vehicle.vin() != null) {
      checkVinIsUnique(vehicle);
    }

    return save(vehicle);
  }

  /**
   * Changes the lifecycle status of a vehicle.
   *
   * @param id vehicle identifier
   * @param status target status
   * @return the updated vehicle
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws IllegalStateException if the status transition is not allowed
   */
  public Vehicle updateStatus(VehicleId id, VehicleStatus status) {
    Objects.requireNonNull(status, "Vehicle status cannot be null");

    Vehicle vehicle = get(id);
    vehicle.transitionTo(status);

    return save(vehicle);
  }

  /**
   * Updates the mileage of a vehicle.
   *
   * @param id vehicle identifier
   * @param mileage new mileage
   * @return the updated vehicle
   * @throws VehicleNotFoundException if the vehicle does not exist
   * @throws IllegalArgumentException if the mileage is negative or lower than the current mileage
   */
  public Vehicle updateMileage(VehicleId id, BigDecimal mileage) {
    Objects.requireNonNull(mileage, "Mileage cannot be null");

    Vehicle vehicle = get(id);
    vehicle.updateMileage(mileage);

    return save(vehicle);
  }

  /**
   * Deletes a vehicle.
   *
   * @param id vehicle identifier
   * @throws VehicleNotFoundException if the vehicle does not exist
   */
  public void delete(VehicleId id) {
    Vehicle vehicle = get(id);
    vehicleRepository.delete(vehicle);
  }

  private Vehicle save(Vehicle vehicle) {
    try {
      return vehicleRepository.save(vehicle);
    } catch (DataIntegrityViolationException exception) {
      throw new VehicleAlreadyExistsException(
          "A vehicle with the same registration number or VIN already exists");
    }
  }

  private void checkRegistrationNumberIsUnique(Vehicle vehicle) {
    if (vehicleRepository.existsByRegistrationNumber(vehicle.registrationNumber())) {
      throw new VehicleAlreadyExistsException(
          "registration number", vehicle.registrationNumber().value());
    }
  }

  private void checkVinIsUnique(Vehicle vehicle) {
    if (vehicle.vin() != null && vehicleRepository.existsByVin(vehicle.vin())) {
      throw new VehicleAlreadyExistsException("VIN", vehicle.vin());
    }
  }

  private static VehicleSpecification specification(
      String brand,
      String model,
      int manufacturingYear,
      VehicleType type,
      FuelType fuelType,
      TransmissionType transmissionType) {
    return new VehicleSpecification(
        brand, model, manufacturingYear, type, fuelType, transmissionType);
  }

  private static void validatePagination(int page, int size) {
    if (page < 0) {
      throw new IllegalArgumentException("Page index cannot be negative");
    }

    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }
  }
}
