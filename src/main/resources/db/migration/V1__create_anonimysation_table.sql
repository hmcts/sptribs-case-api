create table if not exists anonymisation (
   case_reference bigint primary key not null references ccd.case_data(reference) on delete cascade,
   anonymisation_seq bigint not null default 0,
   created_at timestamp not null default now()
);