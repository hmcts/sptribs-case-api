package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class HearingDateSelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("selectHearingDate")
            .label("selectFlagLevel", "<h1>Select hearing</h1>")
            .complex(CaseData::getRecordListing)
            .mandatoryWithLabel(RecordListing::getHearingFormat, "Choose a hearing to cancel")
            .done();
    }
}
