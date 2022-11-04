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
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerRecordListing.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRecordListingTest {
    @InjectMocks
    private CaseworkerRecordListing caseworkerRecordListing;

    @Mock
    private LocationService locationService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerRecordListing.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RECORD_LISTING);
    }

    @Test
    public void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setConferenceCallNumber("");
        recordListing.setHearingType(HearingType.FINAL);
        recordListing.setImportantInfoDetails("some details");
        recordListing.setVideoCallLink("");
        caseData.setRecordListing(recordListing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerRecordListing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getRecordListing().getHearingType().getLabel()).isEqualTo("Final");
        assertThat(response.getData().getRecordListing().getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(stayedResponse).isNotNull();
    }

    @Test
    public void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        when(locationService.populateRegionDynamicList()).thenReturn(getMockedRegionData());
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response.getData().getRecordListing().getRegionList().getValue().getLabel()).isEqualTo("1-region");
        assertThat(response.getData().getRecordListing().getRegionList().getListItems().size()).isEqualTo(1);
        assertThat(response.getData().getRecordListing().getRegionList().getListItems().get(0).getLabel()).isEqualTo("1-region");

    }

    @Test
    public void shouldMidEventMethodSuccessfullyPopulateHearingVenueData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setRegionList(getMockedRegionData());
        caseData.setRecordListing(recordListing);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        when(locationService.getHearingVenuesByRegion("1")).thenReturn(getMockedHearingVenueData());
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        //Then

        assertThat(response.getData().getRecordListing().getHearingVenues().getValue().getLabel()).isEqualTo("venue");
        assertThat(response.getData().getRecordListing().getHearingVenues().getListItems().size()).isEqualTo(1);
        assertThat(response.getData().getRecordListing().getHearingVenues().getListItems().get(0).getLabel()).isEqualTo("venue");

    }

    private DynamicList getMockedRegionData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("1-region")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

    private DynamicList getMockedHearingVenueData() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label("venue")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }
}
