CREATE TABLE IF NOT EXISTS case_document_types
(
  id   BIGINT PRIMARY KEY,
  code VARCHAR(100) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(200)

);

INSERT INTO case_document_types (id, code, name, description)
VALUES
  (1,'APPLICATION', 'Application', 'Document saved through citizen journey'),
  (2,'DOCUMENT_MANAGEMENT', 'Document Management', 'Document saved through document management'),
  (3,'ORDER', 'Order', 'Document via create and send order'),
  (4,'DRAFT_ORDER', 'Draft Order', 'Document from save order'),
  (5,'DECISION', 'Decision', 'Decision on case'),
  (6,'FINAL_DECISION', 'Final Decision', 'Final decision on case'),
  (7,'HEARING_RECORD', 'Hearing Record', 'Audio from the listening on the hearing'),
  (8,'CORRESPONDENCE', 'Correspondence', 'Documents sent out as correspondence'),
  (9,'BUNDLE', 'Bundle', 'Stitched bundle from bundle event'),
  (10,'OTHER', 'Other', 'Other');



