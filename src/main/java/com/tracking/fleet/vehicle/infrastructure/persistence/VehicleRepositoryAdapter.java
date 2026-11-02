package com.tracking.fleet.vehicle.infrastructure.persistence;

import com.tracking.fleet.vehicle.domain.VehicleId;
import com.tracking.fleet.vehicle.domain.VehicleRepository;
import com.tracking.fleet.vehicle.domain.model.RegistrationNumber;
import com.tracking.fleet.vehicle.domain.model.Vehicle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Persistence adapter that implements the domain {@link VehicleRepository} port with Spring Data
 * JPA.
 */
@Repository
public class VehicleRepositoryAdapter implements VehicleRepository {

  private final VehicleJpaRepository jpaRepository;

  /**
   * Creates the persistence adapter.
   *
   * @param jpaRepository Spring Data repository
   */
  public VehicleRepositoryAdapter(VehicleJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Vehicle save(Vehicle vehicle) {
    return jpaRepository.saveAndFlush(VehicleJpaEntity.from(vehicle)).toDomain();
  }

  @Override
  public Optional<Vehicle> findById(VehicleId id) {
    return jpaRepository.findById(id.value()).map(VehicleJpaEntity::toDomain);
  }

  @Override
  public List<Vehicle> findAll(int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    return jpaRepository.findAll(pageable).map(VehicleJpaEntity::toDomain).getContent();
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }

  @Override
  public boolean existsByRegistrationNumber(RegistrationNumber registrationNumber) {
    return jpaRepository.existsByRegistrationNumber(registrationNumber.value());
  }

  @Override
  public boolean existsByVin(String vin) {
    return jpaRepository.existsByVin(vin);
  }

  @Override
  public void delete(Vehicle vehicle) {
    jpaRepository.deleteById(vehicle.id().value());
  }
}
