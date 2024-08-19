package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.ContactPreferenceType;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;
import uk.gov.hmcts.sptribs.common.notification.CancelHearingNotification;
import uk.gov.hmcts.sptribs.testutil.TestEventConstants;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_DATE_1;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.HEARING_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SOLICITOR_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SUBJECT_EMAIL;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getAdditionalHearingDates;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListingWithOneHearingDate;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CANCEL_HEARING;

@ExtendWith(MockitoExtension.class)
class CaseworkerCancelHearingTest {
    @InjectMocks
    private CaseworkerCancelHearing caseworkerCancelHearing;

    @Mock
    private HearingService hearingService;

    @Mock
    private CancelHearingNotification cancelHearingNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCancelHearing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CANCEL_HEARING);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(false);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(false);
    }

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {
        ReflectionTestUtils.setField(caseworkerCancelHearing, "isWorkAllocationEnabled", true);

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCancelHearing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::isPublishToCamunda)
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
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
        when(hearingService.getListedHearingDynamicList(any())).thenReturn(null);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCancelHearing.aboutToStart(updatedCaseDetails);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
    }

    @Test
    void shouldReturnCancelHearingWhenThereAreTwoHearingDates() {
        //Given
        final Listing listing = getRecordListing();
        listing.setAdditionalHearingDate(getAdditionalHearingDates());
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .email(TEST_SUBJECT_EMAIL)
            .contactPreferenceType(ContactPreferenceType.EMAIL)
            .representativeEmailAddress(TEST_SOLICITOR_EMAIL)
            .representativeContactDetailsPreference(ContactPreferenceType.EMAIL)
            .hearingList(getDynamicList())
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .listing(listing)
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled).isNotNull();
        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturnCancelHearingWhenThereAreTwoHearingDatesWithPost() {
        //Given
        final Listing listing = getRecordListing();
        listing.setAdditionalHearingDate(getAdditionalHearingDates());
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(getDynamicList())
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .listing(listing)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled).isNotNull();
        assertThat(response).isNotNull();
    }

    @Test
    void shouldReturnCancelHearingWhenThereIsOneHearingDate() {
        //Given
        final Listing listing = getRecordListingWithOneHearingDate();
        Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .address(SUBJECT_ADDRESS)
            .contactPreferenceType(ContactPreferenceType.POST)
            .representativeContactDetailsPreference(ContactPreferenceType.POST)
            .representativeAddress(SOLICITOR_ADDRESS)
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingList(getDynamicList())
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .listing(listing)
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setState(State.AwaitingOutcome);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCancelHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse cancelled = caseworkerCancelHearing.submitted(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(cancelled.getConfirmationHeader()).contains(NotificationParties.SUBJECT.getLabel());
        assertThat(response).isNotNull();
        assertThat(response.getState()).isEqualTo(State.CaseManagement);
        assertThat(response.getData().getListing().getHearingStatus()).isEqualTo(HearingState.Cancelled);
        assertThat(response.getData().getListing().getCancelledDate()).isEqualTo(LocalDate.now());
    }

    private DynamicList getDynamicList() {
        final DynamicListElement listItem = DynamicListElement
            .builder()
            .label(HEARING_DATE_1 + TestEventConstants.SPACE + HEARING_TIME)
            .code(UUID.randomUUID())
            .build();
        return DynamicList
            .builder()
            .value(listItem)
            .listItems(List.of(listItem))
            .build();
    }

}
