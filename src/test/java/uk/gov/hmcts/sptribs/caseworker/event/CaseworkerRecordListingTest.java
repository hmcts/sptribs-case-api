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
import uk.gov.hmcts.sptribs.caseworker.model.HearingNoticeOption;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingFormat;
import uk.gov.hmcts.sptribs.ciccase.model.HearingType;
import uk.gov.hmcts.sptribs.ciccase.model.RecordListingTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
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
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerRecordListingTest {
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
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        caseData.setRecordListing(getMockRecordListing());
        caseData.setCicCase(getMockCicCase());
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
        assertThat(response.getData().getRecordListing().getHearingNotice()).isEqualTo(HearingNoticeOption.CREATE_FROM_TEMPLATE);
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
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.aboutToStart(updatedCaseDetails);

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
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.midEvent(updatedCaseDetails, beforeDetails);

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

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

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

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerRecordListing.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    private CicCase getMockCicCase() {
        return CicCase.builder().fullName("fullName").recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .representativeFullName("repFullName").recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .respondantName("respName").recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT)).build();
    }

    private RecordListing getMockRecordListing() {
        final RecordListing recordListing = new RecordListing();
        recordListing.setHearingFormat(HearingFormat.FACE_TO_FACE);
        recordListing.setConferenceCallNumber("");
        recordListing.setHearingType(HearingType.FINAL);
        recordListing.setImportantInfoDetails("some details");
        recordListing.setVideoCallLink("");
        recordListing.setHearingNotice(HearingNoticeOption.CREATE_FROM_TEMPLATE);
        recordListing.setTemplate(RecordListingTemplate.HEARING_INVITE_CVP);
        return recordListing;
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
