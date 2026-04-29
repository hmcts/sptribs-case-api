create table if not exists statements (
 id UUID primary key default gen_random_uuid(),
 case_reference_number bigint not null references ccd.case_data(reference) on delete cascade,
 party_type varChar(200) not null,
 document_url varChar(200) not null,
 document_filename varChar(200) not null,
 document_binary_url varChar(200) not null,
 uploaded_at timestamp not null default now()
);
