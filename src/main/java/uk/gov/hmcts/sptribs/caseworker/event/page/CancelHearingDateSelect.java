package uk.gov.hmcts.sptribs.caseworker.event.page;

import io.cucumber.java.bs.A;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class CancelHearingDateSelect implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "cicCaseHearingList=\"NEVER_SHOW\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("caseworkerCancelHearingSelectHearingDate")
            .pageLabel("Select hearing")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingList,"Choose a hearing to cancel")
            .done()
            .complex(CaseData::getRecordListing)
            .readonly(RecordListing::getHearingType, ALWAYS_HIDE)
            .done();

    }
}
