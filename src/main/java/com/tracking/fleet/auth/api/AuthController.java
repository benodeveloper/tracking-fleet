package com.tracking.fleet.auth.api;

import com.tracking.fleet.auth.api.dto.AuthResponse;
import com.tracking.fleet.auth.api.dto.LoginRequest;
import com.tracking.fleet.auth.application.AuthService;
import com.tracking.fleet.shared.api.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication and session profile")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Authenticate a user", operationId = "login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(ApiResponse.success("Login successful", authService.login(request)));
  }

  @GetMapping("/me")
  @Operation(summary = "Get current authenticated user profile", operationId = "me")
  public ResponseEntity<ApiResponse<AuthResponse>> me(Principal principal) {
    return ResponseEntity.ok(ApiResponse.success("Profile retrieved", authService.profile(principal.getName())));
  }
}
