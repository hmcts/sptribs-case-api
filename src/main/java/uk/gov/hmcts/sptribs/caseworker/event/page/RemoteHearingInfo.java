package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

public class RemoteHearingInfo implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
            pageBuilder.page("remoteHearingInformation", this::midEvent)
                .pageLabel("Remote hearing information")
                .label("LabelRemoteHearingInfoObj", "")
                .complex(CaseData::getListing)
                .optionalWithLabel(Listing::getVideoCallLink, "Video link - please do not enter the & character.")
                .optional(Listing::getConferenceCallNumber);
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();

        List<String> errors = new ArrayList<>();

        String videoLinkString = caseData.getListing() != null
            ? caseData.getListing().getVideoCallLink()
            : null;

        if (videoLinkString != null && videoLinkString.contains("&")) {
            errors.add("Video call link must not contain '&'.");
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }


}
