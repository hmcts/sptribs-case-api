package uk.gov.hmcts.sptribs.systemupdate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.CaseDocumentTypesCache;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
@Service
@Slf4j
public class MigrationDocumentService {

    private final DocumentsRepository documentsRepository;
    private final CaseDocumentTypesCache caseDocumentTypesCache;

    @Transactional
    public void buildAndSaveNewDocumentEntityWithDocDateTime(Document document, Long caseReferenceNumber,
                                                             DocumentType documentType, CaseDocumentType caseDocumentType,
                                                             LocalDateTime uploadedTime) {
        OffsetDateTime savedDatetime;

        if (uploadedTime != null) {
            savedDatetime = uploadedTime.atOffset(ZoneOffset.ofHours(0));
        } else {
            savedDatetime = OffsetDateTime.now();
        }

        try {

            int rowsInserted = documentsRepository.insertIgnoreDuplicate(
                caseReferenceNumber,
                document.getUrl(),
                document.getFilename(),
                document.getBinaryUrl(),
                documentType != null ? documentType.name() : null,
                caseDocumentTypesCache.getId(caseDocumentType),
                savedDatetime
            );

            if (rowsInserted == 0) {
                log.info("Document already exists in document table: {}", document.getBinaryUrl());
            }

        } catch (DataAccessException e) {
            throw new DocumentSaveException("Error saving document entity to database", e);
        }
    }
}
