create table if not exists case_documents (
  case_reference_number bigint not null references ccd.case_data(reference) on delete cascade,
  id uuid,
  document_url varChar(200) not null,
  document_binary_url varChar(200) not null,
  document_filename varChar(200) not null,
  category_id varChar(200) not null,
  primary key(case_reference_number, id)
  );
