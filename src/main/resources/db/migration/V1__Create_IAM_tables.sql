-- ============================================================================
-- V1: Identity & Access Management (IAM)
-- Tables: tb_users, tb_refresh_tokens, tb_audit_logs
-- ============================================================================

-- ----------------------------------------------------------------------------
-- tb_users
-- Core authentication table with RBAC via CHECK constraint.
-- password_hash is VARCHAR(255) to accommodate BCrypt hashes.
-- version column supports JPA @Version optimistic locking.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_users (
    id              BIGSERIAL       PRIMARY KEY,
    email           VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(255)    NOT NULL,
    full_name       VARCHAR(255)    NOT NULL,
    role            VARCHAR(30)     NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    version         BIGINT          NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email       UNIQUE (email),
    CONSTRAINT ck_users_role        CHECK (role IN ('ADMIN', 'OPERADOR_ESTOQUE', 'MOTORISTA', 'GERENTE'))
);

COMMENT ON TABLE  tb_users              IS 'System users with role-based access control';
COMMENT ON COLUMN tb_users.password_hash IS 'BCrypt-encoded password hash';
COMMENT ON COLUMN tb_users.role          IS 'RBAC role: ADMIN | OPERADOR_ESTOQUE | MOTORISTA | GERENTE';
COMMENT ON COLUMN tb_users.version       IS 'Optimistic locking version for JPA @Version';

-- ----------------------------------------------------------------------------
-- tb_refresh_tokens
-- Stores JWT refresh tokens with expiration and revocation support.
-- Enables server-side token invalidation (logout / rotation).
-- ----------------------------------------------------------------------------
CREATE TABLE tb_refresh_tokens (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    token           VARCHAR(512)    NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    revoked         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_refresh_tokens_token  UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user   FOREIGN KEY (user_id) REFERENCES tb_users (id) ON DELETE CASCADE
);

COMMENT ON TABLE tb_refresh_tokens IS 'JWT refresh tokens with revocation capability';

-- ----------------------------------------------------------------------------
-- tb_audit_logs
-- Immutable audit trail capturing who did what and when.
-- old_value / new_value store JSON or plain-text diffs for field-level auditing.
-- ----------------------------------------------------------------------------
CREATE TABLE tb_audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT,
    action          VARCHAR(50)     NOT NULL,
    entity_name     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT,
    old_value       TEXT,
    new_value       TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES tb_users (id) ON DELETE SET NULL
);

COMMENT ON TABLE  tb_audit_logs           IS 'Immutable audit trail for all entity changes';
COMMENT ON COLUMN tb_audit_logs.action     IS 'Action type: CREATE, UPDATE, DELETE, LOGIN, etc.';
COMMENT ON COLUMN tb_audit_logs.old_value  IS 'Previous state (JSON or text) before the change';
COMMENT ON COLUMN tb_audit_logs.new_value  IS 'New state (JSON or text) after the change';
