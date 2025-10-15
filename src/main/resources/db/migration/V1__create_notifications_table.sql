create table notifications (
    id bigserial primary key,
    case_reference bigint not null references ccd.case_data(reference) on delete cascade,
    recipient varchar(255) not null,
    sent_at timestamptz default current_timestamp
);
