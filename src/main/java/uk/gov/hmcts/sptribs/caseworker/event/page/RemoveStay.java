package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.RemoveCaseStay;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class RemoveStay implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("removeStay")
            .pageLabel("Remove stay from this case")
            .label("LabelRemoveStay", "")
            .complex(CaseData::getRemoveCaseStay)
            .mandatory(RemoveCaseStay::getStayRemoveReason)
            .mandatory(RemoveCaseStay::getStayRemoveOtherDescription, "removeStayStayRemoveReason = \"Other\"")
            .optional(RemoveCaseStay::getAdditionalDetail, "").done();
    }
}
