CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('ADMIN', 'USER'))
);

CREATE TABLE spaces (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    location VARCHAR(150),
    hourly_rate NUMERIC(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    CONSTRAINT ck_spaces_type CHECK (type IN ('DESK', 'MEETING_ROOM', 'PRIVATE_OFFICE')),
    CONSTRAINT ck_spaces_capacity CHECK (capacity > 0),
    CONSTRAINT ck_spaces_hourly_rate CHECK (hourly_rate >= 0)
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id),
    space_id BIGINT NOT NULL REFERENCES spaces (id),
    start_datetime TIMESTAMPTZ NOT NULL,
    end_datetime TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_price NUMERIC(10, 2) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reservations_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'PENDING_PAYMENT', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_reservations_dates CHECK (end_datetime > start_datetime),
    CONSTRAINT no_overlapping_reservations EXCLUDE USING gist (
        space_id WITH =,
        tstzrange(start_datetime, end_datetime) WITH &&
    ) WHERE (status NOT IN ('CANCELLED'))
);

CREATE INDEX idx_reservations_space_time ON reservations (space_id, start_datetime, end_datetime);
CREATE INDEX idx_reservations_user ON reservations (user_id);
