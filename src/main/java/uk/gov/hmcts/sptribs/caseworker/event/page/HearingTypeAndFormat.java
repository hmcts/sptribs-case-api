package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class HearingTypeAndFormat implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("HearingTypeAndFormat", this::midEvent)
            .pageLabel("Hearing type and format")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("LabelHearingTypeAndFormat", "")
            .complex(CaseData::getListing)
            .mandatory(Listing::getHearingType)
            .mandatory(Listing::getHearingFormat)
            .readonly(Listing::getHearingSummaryExists,ALWAYS_HIDE)
            .done()
            .readonly(CaseData::getHearingList, ALWAYS_HIDE);
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        Listing listing = new Listing();
        listing.setHearingType(details.getData().getListing().getHearingType());
        listing.setHearingFormat(details.getData().getListing().getHearingFormat());
        data.setListing(listing);
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .build();
    }


}
