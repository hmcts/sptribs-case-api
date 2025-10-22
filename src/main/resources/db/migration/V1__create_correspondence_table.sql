create table if not exists case_correspondences (
  case_reference_number varChar(200) not null references ccd.case_data(case_reference_number) on delete cascade,
  id uuid,
  event_type varChar(200) not null,
  sent_on timestamp not null,
  sent_from varChar(200) not null,
  sent_to varChar(200) not null,
  document_url varChar(2000) not null,
  correspondence_type varChar(200) not null,
  primary key(case_reference_number, id)
);
