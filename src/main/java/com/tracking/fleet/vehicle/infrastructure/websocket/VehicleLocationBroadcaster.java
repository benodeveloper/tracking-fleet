package com.tracking.fleet.vehicle.infrastructure.websocket;

import com.tracking.fleet.vehicle.api.dto.VehicleLocationResponse;
import com.tracking.fleet.vehicle.domain.model.VehicleLocation;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class VehicleLocationBroadcaster {

  public static final String TOPIC = "/topic/vehicles/live";

  private final SimpMessagingTemplate messaging;

  public VehicleLocationBroadcaster(SimpMessagingTemplate messaging) {
    this.messaging = messaging;
  }

  public void broadcast(VehicleLocation location) {
    messaging.convertAndSend(TOPIC, VehicleLocationResponse.from(location));
  }
}
