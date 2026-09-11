package uk.gov.hmcts.sptribs.controllers.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.ContactPartyDocumentDetails;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class CaseworkerCICDocumentMapper {

    public CaseworkerCICDocument mapDocument(DocumentEntity entity) {
        return mapDocument(entity, entity.getSavedAt().toLocalDate());
    }

    private CaseworkerCICDocument mapDocument(
        DocumentEntity entity,
        LocalDate date
    ) {
        DocumentType documentType = mapDocumentType(entity.getDocumentTypeName());
        Document document = Document.builder()
            .filename(entity.getDocumentFilename())
            .url(entity.getDocumentUrl())
            .binaryUrl(entity.getDocumentBinaryUrl())
            .categoryId(documentType == null ? null : documentType.getCategory())
            .build();

        return CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(documentType)
            .date(date)
            .build();
    }

    public ListValue<CaseDocumentView> mapCaseDocumentView(
        DocumentEntity entity,
        CaseDocumentType caseDocumentType
    ) {
        CaseworkerCICDocument document = mapDocument(entity);

        return ListValue.<CaseDocumentView>builder()
            .id(String.valueOf(entity.getId()))
            .value(CaseDocumentView.builder()
                .sourceType(caseDocumentType.getLabel())
                .documentCategory(document.getDocumentCategory())
                .documentDate(document.getDate())
                .documentLink(document.getDocumentLink())
                .build())
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

    public DocumentType mapDocumentType(String documentTypeName) {
        if (documentTypeName == null) {
            return null;
        }

        try {
            return DocumentType.valueOf(documentTypeName);
        } catch (IllegalArgumentException exception) {
            log.warn("Unsupported document type name: {}", documentTypeName);
            return null;
        }
    }

}
