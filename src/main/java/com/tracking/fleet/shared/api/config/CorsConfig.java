package com.tracking.fleet.shared.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for browser-based clients (e.g. the separated React frontend).
 *
 * <p>Allowed origins are configurable via {@code app.cors.allowed-origins}. Defaults cover common
 * Vite dev ports.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

  private final String[] allowedOrigins;

  public CorsConfig(
      @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:5174}")
          String[] allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins)
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");
  }
}
