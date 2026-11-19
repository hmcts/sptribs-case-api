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

import static java.util.Objects.isNull;

@Slf4j
@Component
public class HearingTypeAndFormat implements CcdPageConfiguration {

    private static final String ALWAYS_HIDE = "venueNotListedOption=\"ALWAYS_HIDE\"";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("HearingTypeAndFormat", this::midEvent)
            .pageLabel(" Hearing type and format ")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("LabelHearingTypeAndFormat", "")
            .complex(CaseData::getListing)
            .mandatory(Listing::getHearingType)
            .mandatory(Listing::getHearingFormat)
            .readonly(Listing::getHearingSummaryExists,ALWAYS_HIDE)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        final CaseData beforeCaseData = detailsBefore.getData();

        if (!isNull(beforeCaseData.getListing().getHearingType())
                && caseData.getListing().getHearingType() != beforeCaseData.getListing().getHearingType()) {

            String hearingTypeChangeWarning = "The hearing type you have selected for this listing ("
                    + caseData.getListing().getHearingType().getLabel()
                    + ") does not match the hearing type selected in the ‘Case: Hearing options’ event ("
                    + beforeCaseData.getListing().getHearingType().getLabel()
                    + ") - are you sure you want to proceed with this new hearing type?"
                    + " The hearing type set via ‘Case: Hearing options’ will not be retained.";

            caseData.getListing().setHearingTypeChangeWarning(hearingTypeChangeWarning);
        } else {
            caseData.getListing().setHearingTypeChangeWarning("no-warning");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
                .data(caseData)
                .build();
    }

}
