package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class SendOrderOrderDueDates implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerSendOrderOrderDueDates")
            .pageLabel("Add a due date")
            .complex(CaseData::getCicCase)
                .optional(CicCase::getOrderDueDates)
                .optional(CicCase::getAdminActionRequired)
            .done();
    }
}
