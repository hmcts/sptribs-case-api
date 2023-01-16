package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.*;
import uk.gov.hmcts.sptribs.common.notification.ListingUpdatedNotification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_RECORD_LISTING;


@ExtendWith(MockitoExtension.class)
class CaseworkerEditRecordListingTest {

    @InjectMocks
    private CaseworkerEditRecordListing caseworkerEditRecordList;

    @Mock
    private RecordListHelper recordListHelper;

    @Mock
    private ListingUpdatedNotification liistingUpdatedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerEditRecordList.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_RECORD_LISTING);
    }

    @Test
    void shouldSuccessfullyUpdateRecordListingData() {
        //Given
        Set<NotificationParties> hearingNotificationPartiesSet = new HashSet<>();
        hearingNotificationPartiesSet.add(NotificationParties.SUBJECT);
        hearingNotificationPartiesSet.add(NotificationParties.REPRESENTATIVE);
        hearingNotificationPartiesSet.add(NotificationParties.RESPONDENT);

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        CicCase cicCase = getMockCicCase();
        cicCase.setHearingNotificationParties(hearingNotificationPartiesSet);
        caseData.setRecordListing(getRecordListing());
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        Mockito.doNothing().when(liistingUpdatedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(liistingUpdatedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        Mockito.doNothing().when(liistingUpdatedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        SubmittedCallbackResponse stayedResponse = caseworkerEditRecordList.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response.getData().getRecordListing().getHearingType().getLabel()).isEqualTo("Final");
        assertThat(response.getData().getRecordListing().getHearingFormat().getLabel()).isEqualTo("Face to face");
        assertThat(stayedResponse).isNotNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldAboutToStartMethodSuccessfullyPopulateRegionData() {
        //Given
        final CaseData caseData = caseData();
        caseData.getCicCase().setRecordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordList.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response.getState().getName()).isEqualTo("CaseManagement");

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
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.midEvent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(response).isNotNull();

    }

    @Test
    void shouldNotReturnErrorsIfCaseDataIsValid() {
        final CaseData caseData = caseData();

        caseData.getCicCase().setRecordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT));
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).isEmpty();

    }

    @Test
    void shouldReturnErrorsIfCaseDataIsNull() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(recordListHelper.getErrorMsg(any())).thenReturn(List.of("One party must be selected."));
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getErrors()).hasSize(1);

    }

    @Test
    void shouldChangeStateOnAboutToSubmit() {
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

        AboutToStartOrSubmitResponse<CaseData, State> response
            = caseworkerEditRecordList.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response.getState().getName()).isEqualTo("AwaitingHearing");
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

}
