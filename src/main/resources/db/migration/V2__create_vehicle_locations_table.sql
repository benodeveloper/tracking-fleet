CREATE TABLE vehicle_locations (
    id UUID PRIMARY KEY,

    vehicle_id UUID NOT NULL,

    latitude NUMERIC(10, 7) NOT NULL,
    longitude NUMERIC(10, 7) NOT NULL,

    speed_kph NUMERIC(8, 2),
    heading NUMERIC(5, 2),
    accuracy_meters NUMERIC(8, 2),

    recorded_at TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_vehicle_locations_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id)
        ON DELETE CASCADE,

    CONSTRAINT chk_vehicle_locations_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT chk_vehicle_locations_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT chk_vehicle_locations_speed
        CHECK (speed_kph IS NULL OR speed_kph >= 0),

    CONSTRAINT chk_vehicle_locations_heading
        CHECK (heading IS NULL OR (heading >= 0 AND heading < 360)),

    CONSTRAINT chk_vehicle_locations_accuracy
        CHECK (accuracy_meters IS NULL OR accuracy_meters >= 0)
);

CREATE INDEX idx_vehicle_locations_vehicle_recorded_at
    ON vehicle_locations (vehicle_id, recorded_at DESC);

CREATE INDEX idx_vehicle_locations_received_at
    ON vehicle_locations (received_at DESC);
