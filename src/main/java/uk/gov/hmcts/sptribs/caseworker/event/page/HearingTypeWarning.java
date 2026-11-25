package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

public class HearingTypeWarning implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("hearingTypeWarning")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .pageLabel("Are you sure you want to change the hearing type of this listing?")
            .complex(CaseData::getListing)
            .readonly(Listing::getHearingTypeChangeWarning)
            .done();
    }
}
