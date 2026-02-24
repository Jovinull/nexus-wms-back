-- ============================================================================
-- V5: Performance Indexes
-- Creates indexes on columns used in frequent queries, JOINs, and filters.
-- Primary keys and UNIQUE constraints already have implicit indexes.
-- ============================================================================

-- ---- IAM ------------------------------------------------------------------

-- Audit log lookups by user and entity
CREATE INDEX idx_audit_logs_user_id     ON tb_audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity      ON tb_audit_logs (entity_name, entity_id);
CREATE INDEX idx_audit_logs_created_at  ON tb_audit_logs (created_at);

-- Refresh token lookup by user (revocation queries)
CREATE INDEX idx_refresh_tokens_user_id ON tb_refresh_tokens (user_id);

-- ---- WMS ------------------------------------------------------------------

-- Product search and filtering
CREATE INDEX idx_products_name          ON tb_products (name);
CREATE INDEX idx_products_category_id   ON tb_products (category_id);
CREATE INDEX idx_products_abc_curve     ON tb_products (abc_curve);
CREATE INDEX idx_products_active        ON tb_products (active) WHERE active = TRUE;

-- Stock lookups — critical for pick and inventory queries
CREATE INDEX idx_stocks_product_id      ON tb_stocks (product_id);
CREATE INDEX idx_stocks_location_id     ON tb_stocks (location_id);
CREATE INDEX idx_stocks_expiry_date     ON tb_stocks (expiry_date);
CREATE INDEX idx_stocks_batch_number    ON tb_stocks (batch_number);

-- Composite index for FEFO picking strategy (First-Expired, First-Out)
CREATE INDEX idx_stocks_fefo            ON tb_stocks (product_id, expiry_date ASC NULLS LAST);

-- ---- Orders ---------------------------------------------------------------

-- Order filtering by status and date
CREATE INDEX idx_orders_status          ON tb_orders (status);
CREATE INDEX idx_orders_created_at      ON tb_orders (created_at);

-- Composite: dashboard queries (e.g. pending orders today)
CREATE INDEX idx_orders_status_date     ON tb_orders (status, created_at);

-- Order items FK lookups
CREATE INDEX idx_order_items_order_id   ON tb_order_items (order_id);
CREATE INDEX idx_order_items_product_id ON tb_order_items (product_id);

-- Picking list lookups
CREATE INDEX idx_picking_lists_order_id     ON tb_picking_lists (order_id);
CREATE INDEX idx_picking_lists_assigned_to  ON tb_picking_lists (assigned_to);
CREATE INDEX idx_picking_lists_status       ON tb_picking_lists (status);

-- Picking list items FK lookups
CREATE INDEX idx_picking_items_list_id      ON tb_picking_list_items (picking_list_id);
CREATE INDEX idx_picking_items_stock_id     ON tb_picking_list_items (stock_id);

-- ---- TMS ------------------------------------------------------------------

-- Shipment filtering
CREATE INDEX idx_shipments_status       ON tb_shipments (status);
CREATE INDEX idx_shipments_vehicle_id   ON tb_shipments (vehicle_id);
CREATE INDEX idx_shipments_driver_id    ON tb_shipments (driver_id);
CREATE INDEX idx_shipments_departure    ON tb_shipments (departure_time);

-- Shipment-order junction lookups
CREATE INDEX idx_shipment_orders_ship   ON tb_shipment_orders (shipment_id);
CREATE INDEX idx_shipment_orders_order  ON tb_shipment_orders (order_id);
