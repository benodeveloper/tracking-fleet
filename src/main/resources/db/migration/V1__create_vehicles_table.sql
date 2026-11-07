CREATE TABLE vehicles (
    id UUID PRIMARY KEY,

    registration_number VARCHAR(30) NOT NULL,
    vin VARCHAR(17),

    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    manufacturing_year SMALLINT NOT NULL,

    vehicle_type VARCHAR(30) NOT NULL,
    fuel_type VARCHAR(30) NOT NULL,
    transmission_type VARCHAR(30) NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',

    color VARCHAR(50),

    mileage NUMERIC(12, 2) NOT NULL DEFAULT 0.00,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_vehicles_registration_number
        UNIQUE (registration_number),

    CONSTRAINT uk_vehicles_vin
        UNIQUE (vin),

    CONSTRAINT chk_vehicles_manufacturing_year
        CHECK (manufacturing_year BETWEEN 1886 AND 2100),

    CONSTRAINT chk_vehicles_mileage
        CHECK (mileage >= 0),

    CONSTRAINT chk_vehicles_status
        CHECK (status IN (
            'ACTIVE',
            'INACTIVE',
            'UNDER_MAINTENANCE',
            'RETIRED'
        ))
);

CREATE INDEX idx_vehicles_status
    ON vehicles(status);

CREATE INDEX idx_vehicles_brand_model
    ON vehicles(brand, model);
