create sequence if not exists anonymisation_global_seq start with 1 increment by 1;

create table if not exists anonymisation (
   case_reference bigint primary key not null references ccd.case_data(reference) on delete cascade,
   anonymisation_seq bigint not null default nextval('anonymisation_global_seq'),
   created_at timestamp not null default now()
);