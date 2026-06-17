CREATE SCHEMA IF NOT EXISTS ccd;

DROP TABLE IF EXISTS ccd.task_outbox_history;
DROP TABLE IF EXISTS ccd.task_outbox;
DROP TYPE IF EXISTS ccd.task_outbox_status;
DROP TYPE IF EXISTS ccd.task_action;

CREATE TYPE ccd.task_action AS ENUM ('cancel', 'complete', 'initiate', 'reconfigure');
CREATE TYPE ccd.task_outbox_status AS ENUM ('NEW', 'PROCESSING', 'PROCESSED', 'FAILED');

CREATE TABLE ccd.task_outbox (
    id bigserial primary key,
    case_id bigint not null,
    payload jsonb not null,
    requested_action ccd.task_action not null,
    status ccd.task_outbox_status not null default 'NEW',
    attempt_count integer not null default 0,
    created timestamp not null default now(),
    updated timestamp not null default now(),
    next_attempt_at timestamp
);

CREATE TABLE ccd.task_outbox_history (
    id bigserial primary key,
    task_outbox_id bigint not null references ccd.task_outbox(id),
    status ccd.task_outbox_status not null,
    response_code integer,
    error text,
    created timestamp not null default now()
);

CREATE INDEX idx_task_outbox_status_created ON ccd.task_outbox(status, created);
CREATE INDEX idx_task_outbox_status_next_attempt ON ccd.task_outbox(status, next_attempt_at);
CREATE INDEX idx_task_outbox_history_outbox_id_created ON ccd.task_outbox_history(task_outbox_id, created);
