create table if not exists notifications (
    case_reference bigint primary key references ccd.case_data(reference) on delete cascade,
    recipient varchar(255) not null,
    sent_at timestamptz default current_timestamp
);
