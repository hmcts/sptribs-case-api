package uk.gov.hmcts.sptribs.controllers.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

@Data
@Builder
public class DashboardDocument {

    private CaseworkerCICDocument document;
    private boolean downloaded;
}
