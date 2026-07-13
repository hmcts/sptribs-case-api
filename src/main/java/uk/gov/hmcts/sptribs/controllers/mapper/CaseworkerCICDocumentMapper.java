package uk.gov.hmcts.sptribs.controllers.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDate;
import java.util.List;

@Component
public class CaseworkerCICDocumentMapper {

    CaseworkerCICDocument mapDocument(DocumentEntity entity) {
        LocalDate documentDate = entity.getUpdatedAt() != null
            ? entity.getUpdatedAt().toLocalDate()
            : entity.getSavedAt().toLocalDate();

        return mapDocument(entity, documentDate);
    }

    private CaseworkerCICDocument mapDocument(
        DocumentEntity entity,
        LocalDate date
    ) {
        Document document = Document.builder()
            .filename(entity.getDocumentFilename())
            .url(entity.getDocumentUrl())
            .build();

        return CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(mapDocumentType(entity.getDocumentTypeName()))
            .date(date)
            .build();
    }

    public CaseworkerCICDocument mapContactPartyDocument(
        ContactPartyDocumentDetails details
    ) {
        return mapDocument(
            details.document(),
            details.sentOn().toLocalDate()
        );
    }

    public List<CaseworkerCICDocument> mapDocuments(
        List<DocumentEntity> entities
    ) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
            .map(this::mapDocument)
            .toList();
    }

    public List<CaseworkerCICDocument> mapContactPartyDocuments(
        List<ContactPartyDocumentDetails> details
    ) {
        if (details == null) {
            return List.of();
        }

        return details.stream()
            .map(this::mapContactPartyDocument)
            .toList();
    }

    public List<CaseworkerCICDocument> mapDocumentToList(
        DocumentEntity entity
    ) {
        return entity == null
            ? List.of()
            : List.of(mapDocument(entity));
    }

    private DocumentType mapDocumentType(String documentTypeName) {
        return documentTypeName == null
            ? null
            : DocumentType.valueOf(documentTypeName);
    }

}
