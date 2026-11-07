package com.tracking.fleet.auth.application;

import com.tracking.fleet.auth.api.dto.AuthResponse;
import com.tracking.fleet.auth.api.dto.LoginRequest;
import com.tracking.fleet.auth.domain.UserAccount;
import com.tracking.fleet.auth.domain.UserAccountRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserAccountRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public AuthService(
      UserAccountRepository userRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  public AuthResponse login(LoginRequest request) {
    UserAccount user = userRepository.findByUsername(request.username())
        .filter(u -> passwordEncoder.matches(request.password(), u.passwordHash()))
        .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

    String token = jwtService.issueToken(user.username(), user.role());
    return new AuthResponse(
        token, "Bearer", jwtService.expirationSeconds(), user.username(), user.displayName(), user.role());
  }

  public AuthResponse profile(String username) {
    UserAccount user = userRepository.findByUsername(username)
        .orElseThrow(() -> new BadCredentialsException("Invalid token subject"));
    return new AuthResponse(
        null, "Bearer", jwtService.expirationSeconds(), user.username(), user.displayName(), user.role());
  }
}
