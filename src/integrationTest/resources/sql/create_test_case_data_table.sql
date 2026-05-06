CREATE SCHEMA IF NOT EXISTS ccd;

CREATE TABLE ccd.case_data (
                             id BIGINT NOT NULL,
                             reference BIGINT,
                             jurisdiction TEXT,
                             case_type_id TEXT,
                             state TEXT,
                             data JSONB,
                             security_classification TEXT NOT NULL,
                             last_modified TIMESTAMP
);
