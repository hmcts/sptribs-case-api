create type party as ENUM ('APPLICANT', 'REPRESENTATIVE', 'RESPONDENT', 'SUBJECT', 'TRIBUNAL');

alter table case_correspondences
  add constraint uq_correspondence_id unique (id),
  add column receiving_party party;
