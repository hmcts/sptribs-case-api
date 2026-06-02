CREATE TABLE IF NOT EXISTS case_document_types
(
  id           BIGSERIAL PRIMARY KEY,
  code         VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(200) NOT NULL
);

INSERT INTO case_document_types (id, code, name)
VALUES
  (1,'APPLICATION', 'Application'),
  (2,'EVIDENCE', 'Evidence'),
  (3,'CORRESPONDENCE', 'Correspondence'),
  (4,'TRIBUNAL_DOCUMENT', 'Tribunal Document'),
  (5,'HEARING_RECORD', 'Hearing Record'),
  (6,'BUNDLE', 'Bundle');


