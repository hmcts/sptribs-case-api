package uk.gov.hmcts.sptribs.common.repositories.impl;

import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.document.persistence.DocumentEntity;

import java.util.List;

public class DocumentsRepositoryImpl implements DocumentsRepository {
    @Override
    public List<DocumentEntity> getDocumentsForCase(Long ccdReference) {
        return List.of();
    }
}
