CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE SCHEMA IF NOT EXISTS ccd;

DROP TABLE IF EXISTS ccd.task_outbox_history;
DROP TABLE IF EXISTS ccd.task_outbox;
DROP FUNCTION IF EXISTS ccd.notify_task_outbox_complete_finished();
DROP TYPE IF EXISTS ccd.task_outbox_status;
DROP TYPE IF EXISTS ccd.task_action;

CREATE TYPE ccd.task_action AS ENUM ('cancel', 'complete', 'initiate', 'reconfigure');
CREATE TYPE ccd.task_outbox_status AS ENUM ('PENDING', 'PROCESSING', 'PROCESSED', 'UNPROCESSABLE');

CREATE TABLE ccd.task_outbox (
    id bigserial primary key,
    case_id bigint not null,
    event_id varchar(70) not null,
    payload jsonb not null,
    requested_action ccd.task_action not null,
    status ccd.task_outbox_status not null default 'PENDING',
    attempt_count integer not null default 0,
    created timestamp not null default (current_timestamp at time zone 'UTC'),
    updated timestamp not null default (current_timestamp at time zone 'UTC'),
    available_at timestamp default (current_timestamp at time zone 'UTC'),
    claim_token uuid,
    lease_until timestamp,
    constraint task_outbox_event_id_not_blank check (btrim(event_id) <> ''),
    constraint task_outbox_attempt_count_non_negative check (attempt_count >= 0),
    constraint task_outbox_status_shape check (
        (
            status = 'PENDING'::ccd.task_outbox_status
            and available_at is not null
            and claim_token is null
            and lease_until is null
        )
        or (
            status = 'PROCESSING'::ccd.task_outbox_status
            and available_at is null
            and claim_token is not null
            and lease_until is not null
        )
        or (
            status in (
                'PROCESSED'::ccd.task_outbox_status,
                'UNPROCESSABLE'::ccd.task_outbox_status
            )
            and available_at is null
            and claim_token is null
            and lease_until is null
        )
    )
);

CREATE TABLE ccd.task_outbox_history (
    id bigserial primary key,
    task_outbox_id bigint not null references ccd.task_outbox(id) on delete cascade,
    status ccd.task_outbox_status not null,
    response_code integer,
    error text,
    created timestamp not null default (current_timestamp at time zone 'UTC')
);

CREATE UNIQUE INDEX idx_task_outbox_unique_trigger_action
    ON ccd.task_outbox(case_id, event_id, created, requested_action);
CREATE INDEX idx_task_outbox_case_trigger_order
    ON ccd.task_outbox(case_id, created, event_id, id);
CREATE INDEX idx_task_outbox_trigger_action_order
    ON ccd.task_outbox(
        case_id,
        event_id,
        created,
        (
            CASE requested_action
                WHEN 'complete'::ccd.task_action THEN 0
                WHEN 'cancel'::ccd.task_action THEN 10
                WHEN 'reconfigure'::ccd.task_action THEN 20
                WHEN 'initiate'::ccd.task_action THEN 30
            END
        ),
        id
    );
CREATE INDEX idx_task_outbox_pending_available_attempt_id
    ON ccd.task_outbox(available_at, attempt_count, id)
    WHERE status = 'PENDING'::ccd.task_outbox_status;
CREATE INDEX idx_task_outbox_processing_lease_attempt_id
    ON ccd.task_outbox(lease_until, attempt_count, id)
    WHERE status = 'PROCESSING'::ccd.task_outbox_status;
CREATE INDEX idx_task_outbox_case_id_id ON ccd.task_outbox(case_id, id);
CREATE INDEX idx_task_outbox_history_outbox_id ON ccd.task_outbox_history(task_outbox_id, id desc);

CREATE OR REPLACE FUNCTION ccd.notify_task_outbox_complete_finished()
RETURNS trigger
LANGUAGE plpgsql
AS '
BEGIN
    IF new.requested_action = ''complete''::ccd.task_action
       AND old.status = ''PROCESSING''::ccd.task_outbox_status
       AND new.status IN (
           ''PROCESSED''::ccd.task_outbox_status,
           ''UNPROCESSABLE''::ccd.task_outbox_status
       )
    THEN
        PERFORM pg_notify(
            ''task_outbox_complete_finished'',
            json_build_object(
                ''id'', new.id,
                ''case_id'', new.case_id,
                ''requested_action'', new.requested_action,
                ''old_status'', old.status,
                ''new_status'', new.status,
                ''attempt_count'', new.attempt_count,
                ''updated'', new.updated
            )::text
        );
    END IF;

    RETURN new;
END;
';

CREATE TRIGGER trg_task_outbox_complete_finished
AFTER UPDATE OF status ON ccd.task_outbox
FOR EACH ROW
EXECUTE FUNCTION ccd.notify_task_outbox_complete_finished();
