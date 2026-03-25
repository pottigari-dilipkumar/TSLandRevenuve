-- Canonical relational schema for Land Revenue System
-- PostgreSQL 14+

BEGIN;

CREATE TABLE IF NOT EXISTS roles (
    role_id          BIGSERIAL PRIMARY KEY,
    role_name        VARCHAR(50) NOT NULL UNIQUE,
    description      TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    user_id          BIGSERIAL PRIMARY KEY,
    role_id          BIGINT NOT NULL,
    full_name        VARCHAR(150) NOT NULL,
    username         VARCHAR(80) NOT NULL UNIQUE,
    email            VARCHAR(255) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    is_active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES roles(role_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS owner (
    owner_id         BIGSERIAL PRIMARY KEY,
    user_id          BIGINT,
    owner_code       VARCHAR(40) NOT NULL UNIQUE,
    full_name        VARCHAR(150) NOT NULL,
    national_id      VARCHAR(30) UNIQUE,
    phone            VARCHAR(20),
    address_line     TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_owner_user
        FOREIGN KEY (user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS land (
    land_id              BIGSERIAL PRIMARY KEY,
    owner_id             BIGINT NOT NULL,
    land_reference_no    VARCHAR(60) NOT NULL UNIQUE,
    district             VARCHAR(100) NOT NULL,
    tehsil               VARCHAR(100),
    village              VARCHAR(100) NOT NULL,
    survey_number        VARCHAR(60) NOT NULL,
    area_hectare         NUMERIC(12,4) NOT NULL CHECK (area_hectare > 0),
    land_type            VARCHAR(40) NOT NULL,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_land_owner
        FOREIGN KEY (owner_id)
        REFERENCES owner(owner_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS revenue_records (
    record_id              BIGSERIAL PRIMARY KEY,
    land_id                BIGINT NOT NULL,
    collected_by_user_id   BIGINT NOT NULL,
    fiscal_year            VARCHAR(9) NOT NULL,
    assessment_amount      NUMERIC(14,2) NOT NULL CHECK (assessment_amount >= 0),
    amount_paid            NUMERIC(14,2) NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    due_date               DATE NOT NULL,
    payment_date           DATE,
    payment_mode           VARCHAR(30),
    receipt_no             VARCHAR(60) UNIQUE,
    status                 VARCHAR(20) NOT NULL CHECK (status IN ('DUE','PARTIAL','PAID','OVERDUE')),
    remarks                TEXT,
    created_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at             TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_revenue_land
        FOREIGN KEY (land_id)
        REFERENCES land(land_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_revenue_collector
        FOREIGN KEY (collected_by_user_id)
        REFERENCES users(user_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT uq_revenue_land_fiscal_year
        UNIQUE (land_id, fiscal_year)
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_users_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_owner_user_id ON owner(user_id);
CREATE INDEX IF NOT EXISTS idx_land_owner_id ON land(owner_id);
CREATE INDEX IF NOT EXISTS idx_land_location ON land(district, village, survey_number);
CREATE INDEX IF NOT EXISTS idx_revenue_land_id ON revenue_records(land_id);
CREATE INDEX IF NOT EXISTS idx_revenue_collector_id ON revenue_records(collected_by_user_id);
CREATE INDEX IF NOT EXISTS idx_revenue_status_due_date ON revenue_records(status, due_date);
CREATE INDEX IF NOT EXISTS idx_revenue_fiscal_year ON revenue_records(fiscal_year);

COMMIT;
