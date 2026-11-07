package com.tracking.fleet.auth.infrastructure;

import com.tracking.fleet.auth.domain.UserAccount;
import com.tracking.fleet.auth.domain.UserAccountRepository;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountRepositoryAdapter implements UserAccountRepository {

  private final UserAccountJpaRepository jpaRepository;

  public UserAccountRepositoryAdapter(UserAccountJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<UserAccount> findByUsername(String username) {
    return jpaRepository.findByUsernameIgnoreCase(username).map(UserAccountJpaEntity::toDomain);
  }

  @Override
  public UserAccount save(UserAccount user) {
    return jpaRepository.saveAndFlush(UserAccountJpaEntity.from(user)).toDomain();
  }

  @Override
  public long count() {
    return jpaRepository.count();
  }
}
