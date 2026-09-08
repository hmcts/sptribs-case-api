package uk.gov.hmcts.sptribs.controllers.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentView;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

@Component
@RequiredArgsConstructor
public class CaseDocumentProjectionMapper {

    private final CaseworkerCICDocumentMapper caseworkerDocumentMapper;

    public ListValue<CaseDocumentView> mapCaseView(DocumentEntity entity, CaseDocumentType sourceType) {
        CaseworkerCICDocument document = caseworkerDocumentMapper.mapDocument(entity);

        return ListValue.<CaseDocumentView>builder()
            .id(String.valueOf(entity.getId()))
            .value(CaseDocumentView.builder()
                .sourceType(sourceType.getLabel())
                .documentCategory(document.getDocumentCategory())
                .documentDate(document.getDate())
                .documentLink(document.getDocumentLink())
                .build())
            .build();
    }

    public CaseworkerCICDocument mapBundleOrSelection(DocumentEntity entity) {
        return caseworkerDocumentMapper.mapDocument(entity);
    }
}
