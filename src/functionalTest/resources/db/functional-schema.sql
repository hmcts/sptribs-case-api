CREATE SCHEMA IF NOT EXISTS ccd;

CREATE TABLE IF NOT EXISTS ccd.case_data (
    id BIGINT PRIMARY KEY,
    reference BIGINT UNIQUE NOT NULL,
    security_classification VARCHAR(32),
    jurisdiction VARCHAR(70),
    case_type_id VARCHAR(70),
    state VARCHAR(70),
    data JSONB
);

CREATE SEQUENCE IF NOT EXISTS anonymisation_global_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS anonymisation (
   case_reference BIGINT PRIMARY KEY NOT NULL REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
   anonymisation_seq BIGINT NOT NULL DEFAULT nextval('anonymisation_global_seq'),
   created_at TIMESTAMP NOT NULL DEFAULT now()
);
