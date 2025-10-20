create table if not exists case_correspondences (
  case_reference_number bigint not null references ccd.case_data(reference) on delete cascade,
  id bigserial,
  event_type varChar(200) not null,
  sent_on timestamp not null,
  sent_from varChar(200) not null,
  sent_to varChar(200) not null,
  document_url varChar(2000) not null,
  correspondence_type varChar(200) not null,
  primary key(case_reference_number, id)
);
