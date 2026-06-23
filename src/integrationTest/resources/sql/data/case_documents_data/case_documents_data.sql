CREATE SCHEMA IF NOT EXISTS ccd;

INSERT INTO ccd.case_data (id, reference, jurisdiction, case_type_id, state, data, security_classification)
VALUES (1, 1234567890, 'SPTRIBS', 'CriminalInjuriesCompensation', 'Open', '{}'::jsonb, 'PUBLIC');

INSERT INTO case_documents (
  case_reference_number,
  document_url,
  document_binary_url,
  document_filename,
  document_type_name,
  case_document_type_id,
  is_draft,
  sent_to_applicant_via_contact_parties
) VALUES (
           1234567890,
           'test/document/123',
           'test/binary/123',
           'test-document.pdf',
           'Test Document Type',
           1,
           false,
           false
         );
