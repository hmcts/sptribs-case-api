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
import uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;


@ExtendWith(MockitoExtension.class)
class HearingVenuesTest {

    private static final String HEARING_VENUE = "Hearing venue must not contain '&'.";
    private static final String ADDITIONAL_INFO = "Additional instructions and directions must not contain '&'.";

    @InjectMocks
    private HearingVenues hearingVenues;

    @Test
    void whenMidEvent_thenShouldValidateNoSpecialCharacters() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setAddlInstr("Instruct");
        listing.setHearingVenueNameAndAddress("address");
        listing.setVenueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED));
        listing.setReadOnlyHearingVenueName("readOnly");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingVenues.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().getListing().getReadOnlyHearingVenueName()).isNull();

    }

    @Test
     void givenHearingVenueWithSpecialCharacter_whenMidEvent_thenShouldReturnError() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setAddlInstr("Instruct");
        listing.setHearingVenueNameAndAddress("address&&");
        listing.setVenueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED));
        listing.setReadOnlyHearingVenueName("readOnly");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingVenues.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors()).contains(HEARING_VENUE);

    }

    @Test
     void givenAdditionalInfoWithSpecialCharacter_whenMidEvent_thenShouldReturnError() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setAddlInstr("Instruct&&");
        listing.setHearingVenueNameAndAddress("address");
        listing.setVenueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED));
        listing.setReadOnlyHearingVenueName("readOnly");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingVenues.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors()).contains(ADDITIONAL_INFO);

    }

    @Test
     void shouldSetHearingVenueBasedOffDynamicList() {

        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Listing listing = new Listing();
        listing.setAddlInstr("Instruct");
        listing.setHearingVenueNameAndAddress("address");
        listing.setVenueNotListedOption(new HashSet<>());
        listing.setHearingVenues(getMockedHearingVenueData());
        listing.setReadOnlyHearingVenueName("readOnly");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingVenues.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().getListing().getHearingVenueNameAndAddress()).isEqualTo("courtname-courtAddress");

    }

    @Test
    void shouldContainErrorFromInvalidHearingVenue() {
        //given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final Listing listing = new Listing();
        listing.setVenueNotListedOption(Set.of(VenueNotListed.VENUE_NOT_LISTED));
        listing.setHearingVenueNameAndAddress("");
        listing.setReadOnlyHearingVenueName("");

        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();

        caseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response = hearingVenues.midEvent(caseDetails, caseDetails);

        //then
        assertThat(response.getErrors()).contains("Please enter valid Hearing venue");
    }

}
