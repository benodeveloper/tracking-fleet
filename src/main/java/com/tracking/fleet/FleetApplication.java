package com.tracking.fleet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the tracking fleet Spring Boot application.
 */
@SpringBootApplication
public class FleetApplication {

	/**
	 * Creates the application entry point.
	 */
	public FleetApplication() {}

	/**
	 * Starts the application.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(FleetApplication.class, args);
	}

}
