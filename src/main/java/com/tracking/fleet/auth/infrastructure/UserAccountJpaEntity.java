package com.tracking.fleet.auth.infrastructure;

import com.tracking.fleet.auth.domain.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserAccountJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "username", nullable = false, length = 100)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 255)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 150)
  private String displayName;

  @Column(name = "role", nullable = false, length = 30)
  private String role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected UserAccountJpaEntity() {}

  public static UserAccountJpaEntity from(UserAccount user) {
    UserAccountJpaEntity e = new UserAccountJpaEntity();
    e.id = user.id();
    e.username = user.username();
    e.passwordHash = user.passwordHash();
    e.displayName = user.displayName();
    e.role = user.role();
    e.createdAt = user.createdAt();
    e.updatedAt = user.updatedAt();
    return e;
  }

  public UserAccount toDomain() {
    return new UserAccount(id, username, passwordHash, displayName, role, createdAt, updatedAt);
  }
}
