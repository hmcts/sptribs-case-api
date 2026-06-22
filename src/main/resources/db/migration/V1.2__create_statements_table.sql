create table if not exists statements (
  id uuid primary key,
  case_reference_number bigint not null references ccd.case_data(reference) on delete cascade,
  party varchar(100) not null,
  uploaded_on timestamp not null,
  document_url varchar(200) not null,
  document_binary_url varchar(200) not null,
  document_filename varchar(200) not null
);

create index if not exists idx_statements_case_reference_number_uploaded_on
  on statements(case_reference_number, uploaded_on desc);
