package com.tracking.fleet.vehicle.application.command;

import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;

/**
 * Command for replacing the descriptive data of an existing vehicle.
 *
 * @param registrationNumber unique registration number
 * @param vin optional vehicle identification number
 * @param brand vehicle manufacturer
 * @param model vehicle model
 * @param manufacturingYear year of manufacture
 * @param type vehicle category
 * @param fuelType fuel type
 * @param transmissionType transmission type
 * @param color optional color
 */
public record UpdateVehicleCommand(
    String registrationNumber,
    String vin,
    String brand,
    String model,
    int manufacturingYear,
    VehicleType type,
    FuelType fuelType,
    TransmissionType transmissionType,
    String color) {}
