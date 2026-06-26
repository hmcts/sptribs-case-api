CREATE TABLE case_document_types (
                                   id BIGINT PRIMARY KEY,
                                   code VARCHAR(255) NOT NULL,
                                   name VARCHAR(255) NOT NULL,
                                   description VARCHAR(200)

);

INSERT INTO case_document_types (id, code, name, description)
VALUES
  (1,'APPLICANT', 'Applicant', 'Document saved by Applicant on case'),
  (2,'RESPONDENT', 'Respondent', 'Document saved by Respondent on case'),
  (3,'CASEWORKER', 'Caseworker', 'Document saved by Caseworker on case'),
  (4,'ORDER', 'Order', 'Document via create and send order'),
  (5,'DRAFT_ORDER', 'Draft Order', 'Document from save order'),
  (6,'DECISION', 'Decision', 'Decision on case'),
  (7,'FINAL_DECISION', 'Final Decision', 'Final decision on case'),
  (8,'HEARING_RECORD', 'Hearing Record', 'Audio from the listening on the hearing'),
  (9,'CORRESPONDENCE', 'Correspondence', 'Documents sent out as correspondence'),
  (10,'BUNDLE', 'Bundle', 'Stitched bundle from bundle event'),
  (11,'OTHER', 'Other', 'Other');
