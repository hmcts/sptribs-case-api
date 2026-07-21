package uk.gov.hmcts.sptribs.controllers.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;

@Component
public class CaseworkerCICDocumentMapper {

    public CaseworkerCICDocument map(DocumentEntity entity) {

        Document document = Document.builder()
            .filename(entity.getDocumentFilename())
            .url(entity.getDocumentUrl())
            .build();

        return CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(entity.getDocumentTypeName() != null ? DocumentType.valueOf(entity.getDocumentTypeName())
                : null)
            .date(entity.getSavedAt().toLocalDate())
            .build();
    }

    public List<CaseworkerCICDocument> map(List<DocumentEntity> entities) {

        if (entities == null) {
            return List.of();
        }

        return entities.stream()
            .map(this::map)
            .toList();
    }

    public List<CaseworkerCICDocument> mapEntityToList(DocumentEntity entity) {

        if (entity == null) {
            return List.of();
        }

        return List.of(map(entity));
    }
}
