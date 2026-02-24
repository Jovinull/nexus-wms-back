-- ============================================================================
-- V3: Orders & Outbound (Picking / Expedition)
-- Tables: tb_orders, tb_order_items, tb_picking_lists, tb_picking_list_items
-- ============================================================================

-- ----------------------------------------------------------------------------
-- tb_orders
-- Status follows a strict state machine enforced by CHECK constraint:
--   CRIADO -> APROVADO -> EM_SEPARACAO -> SEPARADO -> EM_TRANSITO
--     -> ENTREGUE | CANCELADO | DEVOLVIDO
-- version column supports JPA @Version optimistic locking.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_orders (
    id              BIGSERIAL       PRIMARY KEY,
    customer_name   VARCHAR(255)    NOT NULL,
    customer_address TEXT           NOT NULL,
    customer_phone  VARCHAR(30),
    total_value     NUMERIC(14, 2)  NOT NULL DEFAULT 0,
    status          VARCHAR(30)     NOT NULL DEFAULT 'CRIADO',
    notes           TEXT,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_orders_status CHECK (status IN (
        'CRIADO', 'APROVADO', 'EM_SEPARACAO', 'SEPARADO',
        'EM_TRANSITO', 'ENTREGUE', 'CANCELADO', 'DEVOLVIDO'
    )),
    CONSTRAINT ck_orders_total_value CHECK (total_value >= 0)
);

COMMENT ON TABLE  tb_orders        IS 'Customer orders with state-machine status tracking';
COMMENT ON COLUMN tb_orders.status  IS 'Order lifecycle state: CRIADO → APROVADO → EM_SEPARACAO → SEPARADO → EM_TRANSITO → ENTREGUE/CANCELADO/DEVOLVIDO';
COMMENT ON COLUMN tb_orders.version IS 'Optimistic locking version for concurrent order processing';

-- ----------------------------------------------------------------------------
-- tb_order_items
-- Line items within an order. References the product for price/weight lookups.
-- unit_price is snapshotted at order time (not a live reference to product price).
-- ----------------------------------------------------------------------------
CREATE TABLE tb_order_items (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    product_id      BIGINT          NOT NULL,
    quantity        INTEGER         NOT NULL,
    unit_price      NUMERIC(12, 2)  NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_order_items_quantity   CHECK (quantity > 0),
    CONSTRAINT ck_order_items_price      CHECK (unit_price >= 0),
    CONSTRAINT fk_order_items_order      FOREIGN KEY (order_id)   REFERENCES tb_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product    FOREIGN KEY (product_id) REFERENCES tb_products (id) ON DELETE RESTRICT
);

COMMENT ON TABLE  tb_order_items              IS 'Individual line items within an order';
COMMENT ON COLUMN tb_order_items.unit_price    IS 'Price snapshot at order creation time — decoupled from current product price';

-- ----------------------------------------------------------------------------
-- tb_picking_lists
-- A picking list is generated per order for warehouse operators.
-- Tracks assignment and completion status.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_picking_lists (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    assigned_to     BIGINT,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDENTE',
    started_at      TIMESTAMP,
    completed_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_picking_lists_status CHECK (status IN ('PENDENTE', 'EM_ANDAMENTO', 'CONCLUIDO', 'CANCELADO')),
    CONSTRAINT fk_picking_lists_order  FOREIGN KEY (order_id)    REFERENCES tb_orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_picking_lists_user   FOREIGN KEY (assigned_to) REFERENCES tb_users (id) ON DELETE SET NULL
);

COMMENT ON TABLE  tb_picking_lists             IS 'Picking lists generated per order for warehouse operators';
COMMENT ON COLUMN tb_picking_lists.assigned_to  IS 'Operator (user) responsible for this pick run';

-- ----------------------------------------------------------------------------
-- tb_picking_list_items
-- Individual items to pick, ordered by pick_sequence to optimize
-- the operator's walking route through warehouse aisles.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_picking_list_items (
    id                  BIGSERIAL   PRIMARY KEY,
    picking_list_id     BIGINT      NOT NULL,
    stock_id            BIGINT      NOT NULL,
    product_id          BIGINT      NOT NULL,
    quantity            INTEGER     NOT NULL,
    pick_sequence       INTEGER     NOT NULL DEFAULT 0,
    picked              BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP   NOT NULL DEFAULT NOW(),

    CONSTRAINT ck_picking_items_quantity     CHECK (quantity > 0),
    CONSTRAINT fk_picking_items_list         FOREIGN KEY (picking_list_id) REFERENCES tb_picking_lists (id) ON DELETE CASCADE,
    CONSTRAINT fk_picking_items_stock        FOREIGN KEY (stock_id)        REFERENCES tb_stocks (id) ON DELETE RESTRICT,
    CONSTRAINT fk_picking_items_product      FOREIGN KEY (product_id)      REFERENCES tb_products (id) ON DELETE RESTRICT
);

COMMENT ON TABLE  tb_picking_list_items                IS 'Individual pick instructions ordered by aisle-optimized sequence';
COMMENT ON COLUMN tb_picking_list_items.pick_sequence   IS 'Walking order — items sorted by aisle proximity to minimize travel';
COMMENT ON COLUMN tb_picking_list_items.picked          IS 'Whether the operator has physically collected this item';
