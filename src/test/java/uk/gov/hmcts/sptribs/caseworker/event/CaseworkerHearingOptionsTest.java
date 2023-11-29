package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_HEARING_OPTIONS;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.VenueNotListed.VENUE_NOT_LISTED;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedHearingVenueData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMockedRegionData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerHearingOptionsTest {

    @Mock
    private RecordListHelper recordListHelper;

    @InjectMocks
    private CaseworkerHearingOptions caseworkerHearingOptions;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerHearingOptions.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_HEARING_OPTIONS);
    }

    @Test
    void shouldNotPopulateHearingVenuesDataIfNoRegionSelected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseData.setListing(Listing.builder().build());
        caseDetails.setData(caseData);

        final DynamicList expectedHearingVenuesList =
            DynamicList
                .builder()
                .listItems(emptyList())
                .build();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.midEvent(caseDetails, caseDetails);

        //Then
        verifyNoInteractions(recordListHelper);
        assertThat(response.getData().getListing().getSelectedRegionVal()).isNull();
        assertThat(response.getData().getListing().getHearingVenues()).isEqualTo(expectedHearingVenuesList);
    }

    @Test
    void shouldClearHearingVenuesDataIfChosenRegionValueUnselected() {
        //Given
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseData.setListing(
            Listing.builder()
                .hearingVenues(getMockedHearingVenueData())
                .hearingVenuesMessage("Hearing venues message")
                .build()
        );
        caseDetails.setData(caseData);

        final DynamicList expectedHearingVenuesList =
            DynamicList
                .builder()
                .listItems(emptyList())
                .build();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.midEvent(caseDetails, caseDetails);

        //Then
        verifyNoInteractions(recordListHelper);
        assertThat(response.getData().getListing().getSelectedRegionVal()).isNull();
        assertThat(response.getData().getListing().getHearingVenuesMessage()).isNull();
        assertThat(response.getData().getListing().getHearingVenues()).isEqualTo(expectedHearingVenuesList);
    }

    @Test
    void shouldPopulateHearingVenuesDataWithPreviouslySelectedValuesIfPresent() {
        //Given
        final CaseData caseDataBefore = caseData();
        final Listing recordListing = new Listing();
        recordListing.setHearingVenues(getMockedHearingVenueData());
        recordListing.setRegionList(getMockedRegionData());
        caseDataBefore.setListing(recordListing);
        final CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();
        caseDetailsBefore.setData(caseDataBefore);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = caseData();
        caseData.setListing(Listing.builder().regionList(getMockedRegionData()).build());
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.midEvent(caseDetails, caseDetailsBefore);

        //Then
        verifyNoInteractions(recordListHelper);
        assertThat(response.getData().getListing().getHearingVenues()).isNotNull();
        assertThat(response.getData().getListing().getHearingVenues().getValueLabel())
            .isEqualTo("courtname-courtAddress");
    }

    @Test
    void shouldPopulateHearingVenuesIfNotAlreadyPopulatedAndSavedToCase() {
        //Given
        final CaseData caseData = caseData();
        final Listing recordListing = new Listing();
        recordListing.setRegionList(getMockedRegionData());
        caseData.setListing(recordListing);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();
        caseDetails.setData(caseData);
        final CaseData caseDataBefore = caseData();
        caseDataBefore.setListing(Listing.builder().regionList(getMockedRegionData()).build());
        caseDetailsBefore.setData(caseDataBefore);

        //When
        caseworkerHearingOptions.midEvent(caseDetails, caseDetailsBefore);

        //Then
        verify(recordListHelper).populateVenuesData(caseData);
    }

    @Test
    void shouldPopulateHearingVenuesIfSelectedRegionChangesInEditJourney() {
        //Given
        final CaseData caseData = caseData();
        final Listing recordListing = new Listing();
        recordListing.setRegionList(getMockedRegionData());
        caseData.setListing(recordListing);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();
        caseDetails.setData(caseData);
        final CaseData caseDataBefore = caseData();
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("2-Scotland")
            .code(UUID.randomUUID())
            .build();

        final DynamicList regionList = DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
        caseDataBefore.setListing(Listing.builder().regionList(regionList).build());
        caseDetailsBefore.setData(caseDataBefore);

        //When
        caseworkerHearingOptions.midEvent(caseDetails, caseDetailsBefore);

        //Then
        verify(recordListHelper).populateVenuesData(caseData);
    }

    @Test
    void shouldNotPopulateHearingVenuesIfVenuesArePopulated() {
        //Given
        final DynamicList hearingVenues = getMockedHearingVenueData();
        final CaseData caseData = caseData();
        final Listing recordListing = new Listing();
        recordListing.setRegionList(getMockedRegionData());
        recordListing.setHearingVenues(hearingVenues);
        caseData.setListing(recordListing);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetailsBefore.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.midEvent(caseDetails, caseDetailsBefore);

        //Then
        verifyNoInteractions(recordListHelper);
        assertThat(response.getData().getListing().getHearingVenues())
            .isEqualTo(hearingVenues);
    }

    @Test
    void shouldCallRecordListHelperToPopulateRegionDataIfRegionListIsNull() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        caseworkerHearingOptions.aboutToStart(caseDetails);

        //Then
        verify(recordListHelper).regionData(caseData);
    }

    @Test
    void shouldNotCallRecordListHelperToPopulateRegionDataIfRegionListIsPopulated() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().setRegionList(getMockedRegionData());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        caseworkerHearingOptions.aboutToStart(caseDetails);

        //Then
        verifyNoInteractions(recordListHelper);
    }

    @Test
    void shouldChangeStateOnAboutToSubmitIfCurrentStateIsCaseManagement() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        caseData.getListing().setVenueNotListedOption(Set.of());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(CaseManagement);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.aboutToSubmit(caseDetails, caseDetails);

        //Then
        assertThat(response.getState()).isEqualTo(ReadyToList);
    }

    @Test
    void shouldNotChangeStateOnAboutToSubmitIfCurrentStateIsReadyToList() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        caseData.getListing().setVenueNotListedOption(Set.of());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(ReadyToList);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.aboutToSubmit(caseDetails, caseDetails);

        //Then
        assertThat(response.getState()).isEqualTo(ReadyToList);
    }

    @Test
    void shouldClearHearingVenuesOnAboutToSubmitIfVenueNotListedCheckboxIsSelected() {
        //Given
        final CaseData caseData = caseData();
        caseData.getListing().setHearingVenues(getMockedHearingVenueData());
        caseData.getListing().setVenueNotListedOption(Set.of(VENUE_NOT_LISTED));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final DynamicList expectedHearingVenuesDynamicList = DynamicList
            .builder()
            .listItems(emptyList())
            .build();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.aboutToSubmit(caseDetails, caseDetails);

        //Then
        assertThat(response.getData().getListing().getHearingVenues())
            .isEqualTo(expectedHearingVenuesDynamicList);
    }

    @Test
    void shouldNotClearHearingVenuesOnAboutToSubmitIfVenueNotListedCheckboxIsNotSelected() {
        //Given
        final CaseData caseData = caseData();
        final DynamicList hearingVenues = getMockedHearingVenueData();
        caseData.getListing().setHearingVenues(hearingVenues);
        caseData.getListing().setVenueNotListedOption(Set.of());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerHearingOptions.aboutToSubmit(caseDetails, caseDetails);

        //Then
        assertThat(response.getData().getListing().getHearingVenues())
            .isEqualTo(hearingVenues);
    }
}
