CREATE TABLE owners (
    owner_id UUID PRIMARY KEY,
    full_name TEXT NOT NULL,
    identity_ref_hash TEXT NOT NULL,
    mobile TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE parcels (
    parcel_id TEXT PRIMARY KEY,
    district_code TEXT NOT NULL,
    tehsil_code TEXT NOT NULL,
    village_code TEXT NOT NULL,
    survey_number TEXT NOT NULL,
    area_sq_m NUMERIC(18,2) NOT NULL,
    current_owner_id UUID REFERENCES owners(owner_id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE registrations (
    registration_id UUID PRIMARY KEY,
    parcel_id TEXT NOT NULL REFERENCES parcels(parcel_id),
    seller_owner_id UUID REFERENCES owners(owner_id),
    buyer_owner_id UUID REFERENCES owners(owner_id),
    deed_hash TEXT NOT NULL,
    registration_ref TEXT NOT NULL UNIQUE,
    block_tx_hash TEXT,
    block_number BIGINT,
    status TEXT NOT NULL CHECK (status IN ('SUBMITTED','VERIFIED','CHAIN_ANCHORED','REJECTED')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE mutations (
    mutation_id UUID PRIMARY KEY,
    parcel_id TEXT NOT NULL REFERENCES parcels(parcel_id),
    from_owner_id UUID REFERENCES owners(owner_id),
    to_owner_id UUID REFERENCES owners(owner_id),
    mutation_file_hash TEXT NOT NULL,
    mutation_order_hash TEXT,
    approved_by TEXT,
    status TEXT NOT NULL CHECK (status IN ('PENDING','APPROVED','REJECTED','DISPUTED')),
    block_tx_hash TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE revenue_demands (
    demand_id UUID PRIMARY KEY,
    parcel_id TEXT NOT NULL REFERENCES parcels(parcel_id),
    fiscal_year TEXT NOT NULL,
    amount_due NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) NOT NULL DEFAULT 0,
    due_date DATE,
    status TEXT NOT NULL CHECK (status IN ('OPEN','PARTIAL','PAID','OVERDUE')),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(parcel_id, fiscal_year)
);
