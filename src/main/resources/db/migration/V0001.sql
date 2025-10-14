create table case_correspondence(
   reference bigint references ccd.case_data(reference) ,
   id bigserial,
   timestamp timestamp not null default now(),
   sent_on timestamp not null default now(),
   sent_from varChar(200) not null,
   sent_to varChar(200) not null,
   document_url varChar(2000) not null,
   correspondence_type varChar(200) not null,
   primary key(reference, id)
);

insert into case_correspondence(reference, id, timestamp, sent_on, sent_from, sent_to, document_url, correspondence_type)
select
  reference,
  (correspondence->>'id')::bigint,
  (correspondence->'value'->>'date')::timestamp,
  correspondence->'value'->>'sent_on'::timestamp,
  correspondence->'value'->>'sent_from',
  correspondence->'value'->>'sent_to',
  correspondence->'value'->>'document_url',
  correspondence->'value'->>'correspondence_type'
from
  ccd.case_data,
  jsonb_array_elements(data->'correspondences') correspondence;


create view correspondence_by_case as
select
  reference,
  jsonb_agg(
    json_build_object(
      'value', jsonb_build_object(
         sent_on timestamp,
         sent_from varChar(200),
         sent_to varChar(200),
         document_url varChar(2000),
         correspondence_type varChar(200),
      )
  -- Ensure most recent correspondence is first
  ) order by sent_on desc
) correspondence from case_correspondence
group by reference;
