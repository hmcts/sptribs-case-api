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
public class HearingTypeAndFormat implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "recordVenueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("hearingTypeAndFormat")
            .pageLabel("Hearing type and format")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("labelHearingTypeAndFormat", "")
            .complex(CaseData::getListing)
            .mandatory(Listing::getHearingType)
            .mandatory(Listing::getHearingFormat)
            .readonly(Listing::getHearingSummaryExists,ALWAYS_HIDE)
            .done();
    }


}
