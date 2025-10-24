create table if not exists case_correspondences (
  case_reference_number bigint not null references ccd.case_data(reference) on delete cascade,
  id uuid,
  event_type varChar(200) not null,
  sent_on timestamp not null,
  sent_from varChar(200) not null,
  sent_to varChar(200) not null,
  document_url varChar(200) not null,
  document_binary_url varChar(200) not null,
  document_filename varChar(200) not null,
  correspondence_type varChar(200) not null,
  primary key(case_reference_number, id)
);
