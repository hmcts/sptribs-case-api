package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CLEAR_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingFormat.HYBRID;
import static uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed.VENUE_NOT_LISTED;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerClearHearingOptionsTest {

    @InjectMocks
    private CaseworkerClearHearingOptions caseworkerClearHearingOptions;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerClearHearingOptions.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CLEAR_HEARING_OPTIONS);
    }

    @Test
    void shouldClearHearingOptionsDataInAboutToSubmitCallback() {
        //Given
        final Listing listing = Listing.builder()
            .regionList(getMockedRegionData())
            .hearingVenues(getMockedHearingVenueData())
            .venueNotListedOption(Set.of(VENUE_NOT_LISTED))
            .roomAtVenue("G.10")
            .addlInstr("Hearing to be concluded on the day")
            .hearingFormat(HYBRID)
            .shortNotice(YES)
            .build();
        final CaseData caseData = CaseData.builder()
            .listing(listing)
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> result =
            caseworkerClearHearingOptions.aboutToSubmit(caseDetails, caseDetails);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getData().getListing().getRegionList())
            .isNull();
        assertThat(result.getData().getListing().getHearingVenues())
            .isNull();
        assertThat(result.getData().getListing().getVenueNotListedOption())
            .isNull();
        assertThat(result.getData().getListing().getRoomAtVenue())
            .isNull();
        assertThat(result.getData().getListing().getAddlInstr())
            .isNull();
        assertThat(result.getData().getListing().getHearingFormat())
            .isNull();
        assertThat(result.getData().getListing().getShortNotice())
            .isNull();
    }
}
