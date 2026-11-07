package com.tracking.fleet.auth.infrastructure;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserAccountJpaRepository extends JpaRepository<UserAccountJpaEntity, UUID> {
  Optional<UserAccountJpaEntity> findByUsernameIgnoreCase(String username);
}
