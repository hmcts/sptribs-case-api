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

    private static final String VIDEO_CALL = "Video call link";
    private static final String CONFERENCE_CALL = "Conference call number";

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("remoteHearingInformation", this::midEvent)
            .pageLabel("Remote hearing information")
            .label("LabelRemoteHearingInfoObj", "")
            .complex(CaseData::getListing)
            .optionalWithLabel(Listing::getVideoCallLink, "Video call link - Please do not enter the '&' character.")
            .optionalWithLabel(Listing::getConferenceCallNumber, "Conference call number - Please do not enter the '&' character.");
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        CaseData caseData = details.getData();
        List<String> errors = new ArrayList<>();
        Listing listing = caseData.getListing();

        validateNoSpecialCharacter(listing.getVideoCallLink(), VIDEO_CALL, errors);
        validateNoSpecialCharacter(listing.getConferenceCallNumber(), CONFERENCE_CALL, errors);


        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .errors(errors)
            .build();
    }

    private void validateNoSpecialCharacter(String value, String fieldName, List<String> errors) {
        if (value != null && value.contains("&")) {
            errors.add(fieldName + " must not contain '&'.");
        }
    }


}
