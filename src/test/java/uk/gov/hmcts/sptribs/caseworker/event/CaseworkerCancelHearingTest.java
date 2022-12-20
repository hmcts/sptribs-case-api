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
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.caseworker.util.EventConstants;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerCancelHearing.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListingWithOneHearingDate;

@ExtendWith(MockitoExtension.class)
class CaseworkerCancelHearingTest {
    @InjectMocks
    private CaseworkerCancelHearing caseworkerCancelHearing;

    @Mock
    private HearingService hearingService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerCancelHearing.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CANCEL_HEARING);
    }

    @Test
    void shouldRunAboutToStart() {
        //Given
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        when(hearingService.getHearingDateDynamicList(any())).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCancelHearing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
    }

    @Test
    void shouldReturnCancelHearingWhenThereAreTwoHearingDates() {
        final CicCase cicCase = CicCase.builder()
            .email(TEST_SUBJECT_EMAIL)
            .hearingList(getDynamicList())
            .build();
        final RecordListing recordListing = getRecordListing();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        recordListing.setNotificationParties(parties);
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .recordListing(recordListing)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.hearingCancelled(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled).isNotNull();
        assertThat(response).isNotNull();
        assert (response.getState().getName().equals(State.AwaitingOutcome.getName()));
    }

    @Test
    void shouldReturnCancelHearingWhenThereAreTwoHearingDatesWithPost() {
        final CicCase cicCase = CicCase.builder()
            .address(SUBJECT_ADDRESS)
            .recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(getDynamicList())
            .build();
        final RecordListing recordListing = getRecordListing();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        recordListing.setNotificationParties(parties);
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .recordListing(recordListing)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.hearingCancelled(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled).isNotNull();
        assertThat(response).isNotNull();
        assert (response.getState().getName().equals(State.AwaitingOutcome.getName()));
    }

    @Test
    void shouldReturnCancelHearingWhenThereIsOneHearingDate() {
        final CicCase cicCase = CicCase.builder()
            .address(SUBJECT_ADDRESS)
            .recordNotifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .recordNotifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .recordNotifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(getDynamicList())
            .build();
        final RecordListing recordListing = getRecordListingWithOneHearingDate();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        recordListing.setNotificationParties(parties);
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .recordListing(recordListing)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.hearingCancelled(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled.getConfirmationHeader()).contains(NotificationParties.SUBJECT.getLabel());
        assertThat(response).isNotNull();
        assert (response.getState().getName().equals(State.CaseManagement.getName()));

    }

    private DynamicList getDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(HEARING_DATE_1 + EventConstants.SPACE + HEARING_TIME)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

}
