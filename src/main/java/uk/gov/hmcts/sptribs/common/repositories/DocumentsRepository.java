package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.sptribs.document.persistence.DocumentEntity;

import java.util.List;

public interface DocumentsRepository {

    List<DocumentEntity> getDocumentsForCase(Long ccdReference);
}
