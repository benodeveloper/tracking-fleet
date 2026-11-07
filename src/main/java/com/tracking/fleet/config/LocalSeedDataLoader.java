package com.tracking.fleet.config;

import com.tracking.fleet.auth.domain.UserAccount;
import com.tracking.fleet.auth.domain.UserAccountRepository;
import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleLocationRepository;
import com.tracking.fleet.vehicle.domain.VehicleRepository;
import com.tracking.fleet.vehicle.domain.enums.FuelType;
import com.tracking.fleet.vehicle.domain.enums.TransmissionType;
import com.tracking.fleet.vehicle.domain.enums.VehicleType;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import com.tracking.fleet.vehicle.domain.model.VehicleSpecification;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds deterministic dummy vehicles, GPS locations, admin, and a driver for local development.
 *
 * <p>Runs only with the {@code local} profile and is idempotent.
 */
@Component
@Profile("local")
public class LocalSeedDataLoader implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(LocalSeedDataLoader.class);

  private final VehicleRepository vehicleRepository;
  private final VehicleLocationRepository vehicleLocationRepository;
  private final UserAccountRepository userAccountRepository;
  private final PasswordEncoder passwordEncoder;

  public LocalSeedDataLoader(
      VehicleRepository vehicleRepository,
      VehicleLocationRepository vehicleLocationRepository,
      UserAccountRepository userAccountRepository,
      PasswordEncoder passwordEncoder) {
    this.vehicleRepository = vehicleRepository;
    this.vehicleLocationRepository = vehicleLocationRepository;
    this.userAccountRepository = userAccountRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(String... args) {
    VehicleId tf1001 = seed("TF-1001", "1HGBH41JXMN109186", "Volvo", "FH16", 2022, VehicleType.TRUCK, FuelType.DIESEL,
        TransmissionType.AUTOMATIC, "Blue", 52340.0, List.of(
            point(52.3676, 4.9041, 42.5, 90.0, 4.0, 18),
            point(52.3720, 4.9150, 38.0, 75.0, 5.0, 7),
            point(52.3785, 4.9265, 45.2, 60.0, 3.5, 1)));
    seed("TF-1002", "2FMHK6DT7EBD12345", "Mercedes-Benz", "Sprinter", 2023, VehicleType.VAN,
        FuelType.DIESEL, TransmissionType.MANUAL, "White", 18420.0, List.of(
            point(51.9244, 4.4777, 28.0, 180.0, 6.0, 22),
            point(51.9193, 4.4818, 31.5, 165.0, 4.5, 9),
            point(51.9130, 4.4880, 35.0, 150.0, 4.0, 2)));
    seed("TF-1003", "5YJ3E1EA7PF012345", "Tesla", "Model 3", 2024, VehicleType.CAR, FuelType.ELECTRIC,
        TransmissionType.AUTOMATIC, "Red", 9120.0, List.of(
            point(52.0907, 5.1214, 55.0, 320.0, 3.0, 14),
            point(52.0950, 5.1100, 60.0, 310.0, 3.0, 6),
            point(52.1015, 5.0985, 52.0, 300.0, 2.5, 1)));
    seed("TF-1004", "JHMFA16586S012345", "Iveco", "Daily", 2021, VehicleType.VAN, FuelType.DIESEL,
        TransmissionType.MANUAL, "Silver", 77450.0, List.of(
            point(50.8514, 5.6910, 22.0, 45.0, 7.0, 30),
            point(50.8560, 5.7005, 26.0, 40.0, 6.0, 11),
            point(50.8610, 5.7120, 24.5, 35.0, 5.5, 3)));
    seed("TF-1005", "WAUZZZ8V4JA012345", "MAN", "Lion's City", 2020, VehicleType.BUS, FuelType.CNG,
        TransmissionType.AUTOMATIC, "Yellow", 132880.0, List.of(
            point(53.2194, 6.5665, 18.0, 270.0, 8.0, 25),
            point(53.2188, 6.5530, 20.0, 265.0, 6.5, 10),
            point(53.2180, 6.5400, 19.0, 260.0, 6.0, 2)));
    seedAdmin();
    seedDriver(tf1001);
  }

  private void seedAdmin() {
    if (userAccountRepository.findByUsername("admin").isPresent()) return;
    Instant now = Instant.now();
    userAccountRepository.save(new UserAccount(UUID.randomUUID(), "admin",
        passwordEncoder.encode("admin123"), "Fleet Administrator", "ADMIN", null, now, now));
    log.info("Seeded local admin user (admin / admin123)");
  }

  private void seedDriver(VehicleId assignedVehicleId) {
    if (userAccountRepository.findByUsername("driver").isPresent()) return;
    Instant now = Instant.now();
    UUID vehicleId = assignedVehicleId != null ? assignedVehicleId.value() : null;
    userAccountRepository.save(new UserAccount(UUID.randomUUID(), "driver",
        passwordEncoder.encode("driver123"), "Demo Driver", "DRIVER", vehicleId, now, now));
    log.info("Seeded local driver user (driver / driver123) vehicle={}", vehicleId);
  }

  private VehicleId seed(
      String registration,
      String vin,
      String brand,
      String model,
      int year,
      VehicleType type,
      FuelType fuel,
      TransmissionType transmission,
      String color,
      double mileage,
      List<Point> points) {
    if (vehicleRepository.existsByRegistrationNumber(new RegistrationNumber(registration))) {
      return vehicleRepository.findAll(0, 200).stream()
          .filter(v -> v.registrationNumber().value().equals(registration)).findFirst().map(Vehicle::id).orElse(null);
    }
    Vehicle vehicle = Vehicle.register(
        new RegistrationNumber(registration), vin,
        new VehicleSpecification(brand, model, year, type, fuel, transmission), color);
    vehicle.updateMileage(BigDecimal.valueOf(mileage));
    vehicle = vehicleRepository.save(vehicle);
    VehicleId vehicleId = vehicle.id();
    for (Point p : points) {
      VehicleLocation location = VehicleLocation.record(vehicleId, p.lat, p.lon, p.speed, p.heading,
          p.accuracy, Instant.now().minus(p.minutesAgo, ChronoUnit.MINUTES));
      vehicleLocationRepository.save(location);
    }
    log.info("Seeded vehicle {} ({}) with {} location(s)", registration, vehicleId.value(), points.size());
    return vehicleId;
  }

  private static Point point(double lat, double lon, double speed, double heading, double accuracy,
      int minutesAgo) {
    return new Point(BigDecimal.valueOf(lat), BigDecimal.valueOf(lon), BigDecimal.valueOf(speed),
        BigDecimal.valueOf(heading), BigDecimal.valueOf(accuracy), minutesAgo);
  }

  private record Point(BigDecimal lat, BigDecimal lon, BigDecimal speed, BigDecimal heading,
      BigDecimal accuracy, int minutesAgo) {}
}
