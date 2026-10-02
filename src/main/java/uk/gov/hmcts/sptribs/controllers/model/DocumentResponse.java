package uk.gov.hmcts.sptribs.controllers.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DocumentResponse {

    private List<DashboardDocument> latestCaseBundleDocuments;
    private List<DashboardDocument> contactPartiesDocuments;
    private List<DashboardDocument> orderAndDecisionDocuments;
}
