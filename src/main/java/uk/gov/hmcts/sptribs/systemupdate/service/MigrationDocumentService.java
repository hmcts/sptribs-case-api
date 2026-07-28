package uk.gov.hmcts.sptribs.systemupdate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.exception.document.DocumentSaveException;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
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

            documentsRepository.save(DocumentEntity.builder()
                .caseReferenceNumber(caseReferenceNumber)
                .documentUrl(document.getUrl())
                .documentFilename(document.getFilename())
                .documentBinaryUrl(document.getBinaryUrl())
                .documentTypeName(documentType != null ? documentType.name() : null)
                .caseDocumentTypeId(caseDocumentTypesCache.getId(caseDocumentType))
                .savedAt(savedDatetime)
                .build());

        } catch (DataAccessException e) {
            throw new DocumentSaveException("Error saving document entity to database", e);
        }
    }
}
