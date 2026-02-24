-- ============================================================================
-- V2: Warehouse Management System (WMS)
-- Tables: tb_categories, tb_products, tb_warehouse_locations, tb_stocks
-- ============================================================================

-- ----------------------------------------------------------------------------
-- tb_categories
-- Normalized product categories to avoid string duplication.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_categories (
    id              BIGSERIAL       PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_categories_name UNIQUE (name)
);

COMMENT ON TABLE tb_categories IS 'Normalized product categories';

-- ----------------------------------------------------------------------------
-- tb_products
-- Full product catalog with dimensions (for freight calculation),
-- ABC curve classification, and min stock level for alerts.
-- version column supports JPA @Version optimistic locking.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_products (
    id              BIGSERIAL       PRIMARY KEY,
    sku             VARCHAR(50)     NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    category_id     BIGINT,
    abc_curve       VARCHAR(1),
    weight_kg       NUMERIC(10, 3)  NOT NULL DEFAULT 0,
    height_cm       NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    width_cm        NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    depth_cm        NUMERIC(10, 2)  NOT NULL DEFAULT 0,
    unit_price      NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    min_stock_level INTEGER         NOT NULL DEFAULT 0,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_products_sku       UNIQUE (sku),
    CONSTRAINT ck_products_abc_curve  CHECK (abc_curve IN ('A', 'B', 'C')),
    CONSTRAINT ck_products_weight     CHECK (weight_kg >= 0),
    CONSTRAINT ck_products_dimensions CHECK (height_cm >= 0 AND width_cm >= 0 AND depth_cm >= 0),
    CONSTRAINT ck_products_price      CHECK (unit_price >= 0),
    CONSTRAINT ck_products_min_stock  CHECK (min_stock_level >= 0),
    CONSTRAINT fk_products_category   FOREIGN KEY (category_id) REFERENCES tb_categories (id) ON DELETE SET NULL
);

COMMENT ON TABLE  tb_products              IS 'Product catalog with dimensions and ABC classification';
COMMENT ON COLUMN tb_products.sku           IS 'Stock Keeping Unit — unique product identifier';
COMMENT ON COLUMN tb_products.abc_curve     IS 'ABC inventory classification: A (high value), B (medium), C (low)';
COMMENT ON COLUMN tb_products.weight_kg     IS 'Product weight in kilograms — used for freight calculation';
COMMENT ON COLUMN tb_products.min_stock_level IS 'Minimum stock threshold for low-stock alerts';

-- ----------------------------------------------------------------------------
-- tb_warehouse_locations
-- Positional warehouse addressing: Aisle > Shelf > Level > Bin.
-- Composite UNIQUE prevents duplicate addresses.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_warehouse_locations (
    id              BIGSERIAL       PRIMARY KEY,
    aisle           VARCHAR(10)     NOT NULL,
    shelf           VARCHAR(10)     NOT NULL,
    level           VARCHAR(10)     NOT NULL,
    bin             VARCHAR(10)     NOT NULL,
    max_weight_kg   NUMERIC(10, 2),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_locations_address UNIQUE (aisle, shelf, level, bin)
);

COMMENT ON TABLE  tb_warehouse_locations      IS 'Physical warehouse positions (Aisle > Shelf > Level > Bin)';
COMMENT ON COLUMN tb_warehouse_locations.aisle IS 'Warehouse aisle identifier (e.g. A, B, C)';
COMMENT ON COLUMN tb_warehouse_locations.bin   IS 'Individual storage bin within a shelf level';

-- ----------------------------------------------------------------------------
-- tb_stocks
-- N:N between products and locations. A product can be in multiple locations,
-- each with its own lot number and expiry date.
-- version column is CRITICAL for optimistic locking on concurrent picks.
-- quantity CHECK ensures stock never goes negative at the DB level.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_stocks (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    location_id     BIGINT          NOT NULL,
    quantity        INTEGER         NOT NULL DEFAULT 0,
    batch_number    VARCHAR(50),
    expiry_date     DATE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_stocks_quantity    CHECK (quantity >= 0),
    CONSTRAINT fk_stocks_product    FOREIGN KEY (product_id)  REFERENCES tb_products (id) ON DELETE RESTRICT,
    CONSTRAINT fk_stocks_location   FOREIGN KEY (location_id) REFERENCES tb_warehouse_locations (id) ON DELETE RESTRICT
);

COMMENT ON TABLE  tb_stocks               IS 'Positional stock records — one row per product-location-lot combination';
COMMENT ON COLUMN tb_stocks.quantity       IS 'Current quantity in this location — protected by optimistic locking';
COMMENT ON COLUMN tb_stocks.batch_number   IS 'Lot/batch identifier for traceability';
COMMENT ON COLUMN tb_stocks.expiry_date    IS 'Product expiration date — enables FIFO/FEFO picking strategies';
COMMENT ON COLUMN tb_stocks.version        IS 'Optimistic locking version — prevents concurrent pick conflicts';
