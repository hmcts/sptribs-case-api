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
import uk.gov.hmcts.sptribs.caseworker.model.NoticeOption;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.*;
import uk.gov.hmcts.sptribs.recordlisting.LocationService;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerRecordListing.CASEWORKER_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.*;

@ExtendWith(MockitoExtension.class)
class CaseworkerEditRecordListingTest {
    @InjectMocks
    private CaseworkerEditRecordListing caseworkerEditRecordListing;

    @Mock
    private LocationService locationService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerEditRecordListing.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_RECORD_LISTING);
    }

    @Test
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        final CicCase cicCase = CicCase.builder()
            .recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setRecordListing(getRecordListing());
        caseData.setCicCase(getMockCicCase());
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse stayedResponse = caseworkerEditRecordListing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getRecordListing().getHearingType().getLabel()).isEqualTo("Final");
        assertThat(response.getData().getRecordListing().getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(response.getData().getRecordListing().getHearingNotice()).isEqualTo(NoticeOption.CREATE_FROM_TEMPLATE);
        assertThat(response.getData().getRecordListing().getTemplate()).isEqualTo(RecordListingTemplate.HEARING_INVITE_CVP);
        assertThat(stayedResponse).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        when(locationService.getAllRegions()).thenReturn(getMockedRegionData());
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordListing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response.getData().getRecordListing().getRegionList().getValue().getLabel()).isEqualTo("1-region");
        assertThat(response.getData().getRecordListing().getRegionList().getListItems()).hasSize(1);
        assertThat(response.getData().getRecordListing().getRegionList().getListItems().get(0).getLabel()).isEqualTo("1-region");

    }

    @Test
    void shouldMidEventMethodSuccessfullyPopulateHearingVenueData() {
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
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordListing.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getRecordListing().getHearingVenues()
            .getValue().getLabel()).isEqualTo("courtname-courtAddress");
        assertThat(response.getData().getRecordListing().getHearingVenues().getListItems()).hasSize(1);
        assertThat(response.getData().getRecordListing().getHearingVenues()
            .getListItems().get(0).getLabel()).isEqualTo("courtname-courtAddress");

    }

    @Test
    void shouldReturnErrorsIfCaseDataIsNull() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        caseData.setCicCase(CicCase.builder().build());
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldReturnErrorsIfAllNotificationPartiesSelected() {
        final CicCase cicCase = CicCase.builder()
            .recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getData().getRecordListing().getNotificationParties()).hasSize(3);
        assertThat(response.getData().getRecordListing().getNotificationParties()).contains(NotificationParties.SUBJECT);
        assertThat(response.getData().getRecordListing().getNotificationParties()).contains(NotificationParties.SUBJECT);
    }

    private CicCase getMockCicCase() {
        return CicCase.builder().fullName("fullName").recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .representativeFullName("repFullName").recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .respondantName("respName").recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
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
            .label("courtname-courtAddress")
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

}
