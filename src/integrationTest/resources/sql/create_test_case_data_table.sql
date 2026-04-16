CREATE SCHEMA IF NOT EXISTS ccd;

CREATE TABLE ccd.case_data (
                             id BIGSERIAL,
                             reference BIGINT,
                             jurisdiction TEXT,
                             case_type_id TEXT,
                             state TEXT,
                             data JSONB,
                             last_modified TIMESTAMP
);
