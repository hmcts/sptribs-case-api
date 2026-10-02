create table if not exists correspondence_document (
  document_id bigint not null references case_documents(id) on delete cascade,
  correspondence_id uuid references case_correspondences(id) on delete cascade,
  primary key(document_id, correspondence_id)
);
