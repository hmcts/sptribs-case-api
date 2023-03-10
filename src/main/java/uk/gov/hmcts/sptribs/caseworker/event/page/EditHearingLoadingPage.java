package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class EditHearingLoadingPage implements CcdPageConfiguration {

    private static final String SHOW = "recordHearingSummaryExists != \"YES\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editHearingSummary")
            .pageLabel("Edit hearing summary")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("labelEditHearingSummary", "")
            .complex(CaseData::getListing)
            .readonly(Listing::getHearingSummaryExists)
            .label("labelEditHearingSummary", "There are no Completed Hearings to edit", SHOW)
            .done();
    }


}
