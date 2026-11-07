package com.tracking.fleet.auth.domain;

import java.util.Optional;

public interface UserAccountRepository {
  Optional<UserAccount> findByUsername(String username);
  UserAccount save(UserAccount user);
  long count();
}
