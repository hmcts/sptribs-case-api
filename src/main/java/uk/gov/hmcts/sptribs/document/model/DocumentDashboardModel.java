package uk.gov.hmcts.sptribs.document.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentDashboardModel {

    DocumentEntity latestCaseBundleDocument;
    List<DocumentEntity> contactPartiesDocuments;
    List<DocumentEntity> orderAndDecisionDocuments;

}
