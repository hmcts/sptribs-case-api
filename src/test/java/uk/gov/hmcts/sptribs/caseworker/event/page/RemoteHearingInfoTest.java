package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class RemoteHearingInfoTest {

    private static final String VIDEO_ERROR_MESSAGE = "Video call link must not contain '&'.";
    private static final String CONFERENCE_ERROR_MESSAGE = "Conference call number must not contain '&'.";

    @InjectMocks
    private RemoteHearingInfo remoteHearingInfo;

    @Test
    void whenMidEvent_thenShouldValidateNoSpecialCharacters() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setVideoCallLink("link");
        listing.setConferenceCallNumber("123333");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when

        final AboutToStartOrSubmitResponse<CaseData, State> response = remoteHearingInfo.midEvent(caseDetails,caseDetails);

        //then

        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    public void givenVideoLinkWithSpecialCharacter_whenMidEvent_thenShouldReturnError() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setVideoCallLink("link&");
        listing.setConferenceCallNumber("123333");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when

        final AboutToStartOrSubmitResponse<CaseData, State> response = remoteHearingInfo.midEvent(caseDetails,caseDetails);

        //then

        assertThat(response.getErrors()).contains(VIDEO_ERROR_MESSAGE);

    }

    @Test
    public void givenConferenceCallNumberWithSpecialCharacter_whenMidEvent_thenShouldReturnError() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setVideoCallLink("link");
        listing.setConferenceCallNumber("1233&33");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when

        final AboutToStartOrSubmitResponse<CaseData, State> response = remoteHearingInfo.midEvent(caseDetails,caseDetails);

        //then

        assertThat(response.getErrors()).contains(CONFERENCE_ERROR_MESSAGE);

    }

}
