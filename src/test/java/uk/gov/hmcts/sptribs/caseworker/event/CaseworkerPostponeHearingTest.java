package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.event.page.PostponeHearingNotifyParties;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.notification.HearingPostponedNotification;
import uk.gov.hmcts.sptribs.notification.exception.NotificationException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_POSTPONE_HEARING;

@ExtendWith(MockitoExtension.class)
class CaseworkerPostponeHearingTest {
    @InjectMocks
    private CaseworkerPostponeHearing caseworkerPostponeHearing;

    @Mock
    private RecordListHelper recordListHelper;

    @Mock
    private HearingService hearingService;

    @Mock
    private HearingPostponedNotification hearingPostponedNotification;

    @InjectMocks
    private PostponeHearingNotifyParties postponeHearingNotifyParties;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerPostponeHearing.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_POSTPONE_HEARING);
    }

    @Test
    void shouldRunAboutToStart() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        when(hearingService.getListedHearingDynamicList(any())).thenReturn(null);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerPostponeHearing.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_POSTPONE_HEARING);
    }

    @Test
    void shouldReturnErrorsIfNoNotificationPartySelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = postponeHearingNotifyParties.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
    }

    @Test
    void shouldSuccessfullyPostpone() {
        final Set<NotificationParties> parties = new HashSet<>();
        parties.add(NotificationParties.SUBJECT);
        parties.add(NotificationParties.RESPONDENT);
        parties.add(NotificationParties.REPRESENTATIVE);
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .hearingNotificationParties(parties)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        doNothing().when(hearingPostponedNotification).sendToSubject(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(hearingPostponedNotification).sendToRepresentative(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(hearingPostponedNotification).sendToRespondent(caseData, caseData.getHyphenatedCaseRef());
        doNothing().when(recordListHelper).getNotificationParties(any());

        final AboutToStartOrSubmitResponse<CaseData, State> response
            = caseworkerPostponeHearing.aboutToSubmit(updatedCaseDetails, beforeDetails);
        final SubmittedCallbackResponse submitted = caseworkerPostponeHearing.submitted(updatedCaseDetails, beforeDetails);

        assertThat(submitted.getConfirmationHeader()).contains("Hearing Postponed");
        assertThat(response.getData().getListing().getHearingStatus()).isEqualTo(HearingState.Postponed);
        assertThat(response.getData().getListing().getPostponeDate()).isEqualTo(LocalDate.now());
    }

    @ParameterizedTest
    @MethodSource("notificationExceptionCicCase")
    void submittedShouldThrowExceptionWhenSendIsUnsuccessful(String notifyParty, CicCase cicCase, Exception exception) {
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .hyphenatedCaseRef("1234-5678-3456")
            .build();

        final CaseDetails<CaseData, State> beforeCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        if (notifyParty.equals(SubjectCIC.SUBJECT.name())) {
            doThrow(exception).when(hearingPostponedNotification).sendToSubject(any(CaseData.class), anyString());
        }
        if (notifyParty.equals(RepresentativeCIC.REPRESENTATIVE.name())) {
            doThrow(exception).when(hearingPostponedNotification).sendToRepresentative(any(CaseData.class), anyString());
        }
        if (notifyParty.equals(RespondentCIC.RESPONDENT.name())) {
            doThrow(exception).when(hearingPostponedNotification).sendToRespondent(any(CaseData.class), anyString());
        }

        final SubmittedCallbackResponse response = caseworkerPostponeHearing.submitted(updatedCaseDetails, beforeCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader())
            .isEqualTo(format("# Postpone hearing notification failed %n## Please resend the notification"));
    }

    private static Stream<Arguments> notificationExceptionCicCase() {
        final CicCase cicCaseSubject = CicCase.builder()
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CicCase cicCaseRepresentative = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .build();
        final CicCase cicCaseRespondent = CicCase.builder()
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        final Exception sendToSubjectException = new NotificationException(new Exception("Failed to send to subject"));
        final Exception sendToRepresentativeException = new NotificationException(new Exception("Failed to send to representative"));
        final Exception sendToRespondentException = new NotificationException(new Exception("Failed to send to respondent"));

        return Stream.of(
            Arguments.arguments(SubjectCIC.SUBJECT.name(), cicCaseSubject, sendToSubjectException),
            Arguments.arguments(RepresentativeCIC.REPRESENTATIVE.name(), cicCaseRepresentative, sendToRepresentativeException),
            Arguments.arguments(RespondentCIC.RESPONDENT.name(), cicCaseRespondent, sendToRespondentException)
        );
    }
}
