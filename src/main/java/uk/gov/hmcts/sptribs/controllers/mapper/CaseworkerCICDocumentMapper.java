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
            .documentCategory(DocumentType.fromCategory(entity.getCategoryId()))
            .date(entity.getSavedAt().toLocalDate())
            .build();
    }

    public List<CaseworkerCICDocument> map(List<DocumentEntity> entities) {
        return entities.stream()
            .map(this::map)
            .toList();
    }
}
