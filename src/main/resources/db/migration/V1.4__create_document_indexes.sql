CREATE INDEX idx_case_docs_case_ref_type_saved_at
  ON case_documents (
                     case_reference_number,
                     case_document_type_id,
                     saved_at DESC
    );
