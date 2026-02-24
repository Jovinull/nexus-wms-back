-- ============================================================================
-- V4: Transportation Management System (TMS)
-- Tables: tb_vehicles, tb_drivers, tb_shipments, tb_shipment_orders
-- ============================================================================

-- ----------------------------------------------------------------------------
-- tb_vehicles
-- Fleet registry. max_weight_kg and max_volume_m3 are used to validate
-- that shipment loads don't exceed vehicle capacity.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_vehicles (
    id              BIGSERIAL       PRIMARY KEY,
    license_plate   VARCHAR(20)     NOT NULL,
    model           VARCHAR(100)    NOT NULL,
    max_weight_kg   NUMERIC(10, 2)  NOT NULL,
    max_volume_m3   NUMERIC(10, 2)  NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_vehicles_plate        UNIQUE (license_plate),
    CONSTRAINT ck_vehicles_weight       CHECK (max_weight_kg > 0),
    CONSTRAINT ck_vehicles_volume       CHECK (max_volume_m3 > 0)
);

COMMENT ON TABLE  tb_vehicles                IS 'Fleet registry with load capacity constraints';
COMMENT ON COLUMN tb_vehicles.max_weight_kg   IS 'Maximum cargo weight in kilograms';
COMMENT ON COLUMN tb_vehicles.max_volume_m3   IS 'Maximum cargo volume in cubic meters';

-- ----------------------------------------------------------------------------
-- tb_drivers
-- Driver registry with unique CNH (Brazilian driver's license).
-- Drivers are linked to vehicles per-shipment, not via a fixed FK here,
-- keeping the relationship flexible (a driver can use different vehicles).
-- ----------------------------------------------------------------------------
CREATE TABLE tb_drivers (
    id              BIGSERIAL       PRIMARY KEY,
    full_name       VARCHAR(255)    NOT NULL,
    cnh             VARCHAR(20)     NOT NULL,
    cnh_category    VARCHAR(5)      NOT NULL,
    phone           VARCHAR(30),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_drivers_cnh          UNIQUE (cnh),
    CONSTRAINT ck_drivers_cnh_category CHECK (cnh_category IN ('A', 'B', 'C', 'D', 'E', 'AB', 'AC', 'AD', 'AE'))
);

COMMENT ON TABLE  tb_drivers              IS 'Driver registry with unique CNH';
COMMENT ON COLUMN tb_drivers.cnh           IS 'Carteira Nacional de Habilitação — unique driver license number';
COMMENT ON COLUMN tb_drivers.cnh_category  IS 'License category: A, B, C, D, E, AB, AC, AD, AE';

-- ----------------------------------------------------------------------------
-- tb_shipments
-- A shipment (manifesto de carga) groups N orders into one vehicle + driver.
-- Status tracks the delivery lifecycle.
-- version supports optimistic locking for concurrent status updates.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_shipments (
    id              BIGSERIAL       PRIMARY KEY,
    vehicle_id      BIGINT          NOT NULL,
    driver_id       BIGINT          NOT NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'AGUARDANDO',
    departure_time  TIMESTAMP,
    arrival_time    TIMESTAMP,
    notes           TEXT,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_shipments_status CHECK (status IN (
        'AGUARDANDO', 'EM_CARREGAMENTO', 'EM_TRANSITO', 'ENTREGUE', 'CANCELADO'
    )),
    CONSTRAINT fk_shipments_vehicle FOREIGN KEY (vehicle_id) REFERENCES tb_vehicles (id) ON DELETE RESTRICT,
    CONSTRAINT fk_shipments_driver  FOREIGN KEY (driver_id)  REFERENCES tb_drivers (id) ON DELETE RESTRICT
);

COMMENT ON TABLE  tb_shipments         IS 'Shipment manifests linking orders to a vehicle and driver';
COMMENT ON COLUMN tb_shipments.status   IS 'Shipment lifecycle: AGUARDANDO → EM_CARREGAMENTO → EM_TRANSITO → ENTREGUE/CANCELADO';
COMMENT ON COLUMN tb_shipments.version  IS 'Optimistic locking version for concurrent status updates';

-- ----------------------------------------------------------------------------
-- tb_shipment_orders
-- Junction table: which orders are loaded into which shipment.
-- UNIQUE constraint prevents the same order from being added twice.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_shipment_orders (
    id              BIGSERIAL       PRIMARY KEY,
    shipment_id     BIGINT          NOT NULL,
    order_id        BIGINT          NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_shipment_orders       UNIQUE (shipment_id, order_id),
    CONSTRAINT fk_shipment_orders_ship  FOREIGN KEY (shipment_id) REFERENCES tb_shipments (id) ON DELETE CASCADE,
    CONSTRAINT fk_shipment_orders_order FOREIGN KEY (order_id)    REFERENCES tb_orders (id) ON DELETE RESTRICT
);

COMMENT ON TABLE tb_shipment_orders IS 'Junction table linking orders to shipments (N:N)';
