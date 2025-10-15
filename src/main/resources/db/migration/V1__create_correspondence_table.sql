create table if not exists case_correspondence (
  case_reference bigint not null references ccd.case_data(reference) on delete cascade,
  id bigserial,
  timestamp timestamp not null default now(),
  sent_on timestamp not null default now(),
  sent_from varChar(200) not null,
  sent_to varChar(200) not null,
  document_url varChar(2000) not null,
  correspondence_type varChar(200) not null,
  primary key(case_reference, id)
);
