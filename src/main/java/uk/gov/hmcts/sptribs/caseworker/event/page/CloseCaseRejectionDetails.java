package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

public class CloseCaseRejectionDetails implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        Map<String, String> map = new HashMap<>();
        map.put("closeCaseWithdrawalDetails", "closeCloseCaseReason = \"caseWithdrawn\"");
        map.put("closeCaseRejectionDetails", "closeCloseCaseReason = \"caseRejected\"");
        pageBuilder.page("closeCaseRejectionDetails")
            .pageLabel("Rejection details")
            .pageShowConditions(map)
            .complex(CaseData::getCloseCase)
            .mandatory(CloseCase::getRejectionFullName)
            .mandatory(CloseCase::getRejectionReason)
            .mandatory(CloseCase::getRejectionDetails, "closeRejectionReason = \"other\"")
            .done();
    }

}
