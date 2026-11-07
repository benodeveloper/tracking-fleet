ALTER TABLE vehicles
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE vehicles
    DROP CONSTRAINT uk_vehicles_registration_number;

ALTER TABLE vehicles
    DROP CONSTRAINT uk_vehicles_vin;

CREATE UNIQUE INDEX uk_vehicles_registration_number_active
    ON vehicles (registration_number)
    WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX uk_vehicles_vin_active
    ON vehicles (vin)
    WHERE deleted_at IS NULL;

CREATE INDEX idx_vehicles_active_created_at
    ON vehicles (created_at DESC)
    WHERE deleted_at IS NULL;
