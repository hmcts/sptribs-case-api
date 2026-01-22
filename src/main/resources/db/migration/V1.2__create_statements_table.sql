CREATE TABLE IF NOT EXISTS case_statements (
                                             id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                             case_reference_number BIGINT NOT NULL
                                             REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
  party_type VARCHAR(200) NOT NULL,
  created_on TIMESTAMP NOT NULL,
  document_url VARCHAR(200) NOT NULL,
  document_binary_url VARCHAR(200) NOT NULL,
  document_filename VARCHAR(200) NOT NULL,
  CONSTRAINT uq_case_statements_party_filename
  UNIQUE (party_type, document_filename)
  );
