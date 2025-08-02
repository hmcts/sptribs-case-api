package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class AmendOrderDueDates implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("caseworkerAmendDueDateEditDueDate")
            .pageLabel("Amend due dates")
            .label("LabelAmendDueDates", "")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getOrderDueDates)
            .done();
    }
}
