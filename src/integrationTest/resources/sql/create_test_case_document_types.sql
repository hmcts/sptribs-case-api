CREATE TABLE case_document_types (
                                   id BIGINT PRIMARY KEY,
                                   code VARCHAR(255) NOT NULL,
                                   name VARCHAR(255) NOT NULL
);

INSERT INTO case_document_types (id, code, name)
VALUES
  (1,'APPLICATION', 'Application'),
  (2,'EVIDENCE', 'Evidence'),
  (3,'CORRESPONDENCE', 'Correspondence'),
  (4,'TRIBUNAL_DOCUMENT', 'Tribunal Document'),
  (5,'HEARING_RECORD', 'Hearing Record'),
  (6,'BUNDLE', 'Bundle');
