create table if not exists case_documents (
  id serial primary key,
  case_reference_number bigint not null,
  saved_at timestamp not null default current_timestamp,
  document_url varChar(200) not null,
  document_binary_url varChar(200) not null unique,
  document_filename varChar(200) not null,
  category_id varChar(200) not null,
  is_draft boolean not null
  );
