package uk.gov.hmcts.sptribs.controllers.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;

@Data
@Builder
public class DocumentResponse {

    List<CaseworkerCICDocument> latestCaseBundleDocuments;
    List<CaseworkerCICDocument> contactPartiesDocuments;
    List<CaseworkerCICDocument> orderAndDecisionDocuments;
}
