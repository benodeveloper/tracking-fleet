ALTER TABLE users DROP CONSTRAINT chk_users_role;
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'DISPATCHER', 'VIEWER', 'DRIVER'));

ALTER TABLE users ADD COLUMN assigned_vehicle_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_assigned_vehicle
        FOREIGN KEY (assigned_vehicle_id)
        REFERENCES vehicles (id)
        ON DELETE SET NULL;

CREATE INDEX idx_users_assigned_vehicle_id ON users(assigned_vehicle_id);
