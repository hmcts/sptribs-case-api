create table if not exists notifications (
    case_reference bigint primary key references(ccd.case_data) on delete cascade,
    recipient varchar(255) not null,
    sent_at timestamptz,
    references ccd.case_data(reference)
);
