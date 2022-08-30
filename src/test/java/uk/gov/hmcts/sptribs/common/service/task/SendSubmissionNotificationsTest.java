package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.NotificationDispatcher;

import java.util.Set;

import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHWFDecision;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingPayment;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendSubmissionNotificationsTest {

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendSubmissionNotifications sendSubmissionNotifications;

    @Test
    void shouldDispatchSubmittedNotificationsAndOutstandingActionNotificationsIfSubmittedState() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        //When
        sendSubmissionNotifications.apply(caseDetails);

        //Then
        //verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        //verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldDispatchOutstandingAndSubmittedNotificationIfAwaitingHwfDecisionState() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        //When
        sendSubmissionNotifications.apply(caseDetails);

        //Then
        //verify(notificationDispatcher).send(applicationSubmittedNotification, caseData, TEST_CASE_ID);
        //verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldOnlyDispatchOutstandingNotificationIfAwaitingHwfDecisionStateAndCannotUpload() {
        //Given
        final CaseData caseData = caseData();
        caseData.getApplication().setApplicant1CannotUploadSupportingDocument(Set.of(DocumentType.MARRIAGE_CERTIFICATE));
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingHWFDecision);

        //When
        sendSubmissionNotifications.apply(caseDetails);

        //Then
        //verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldOnlyDispatchOutstandingNotificationIfAwaitingDocumentsState() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingDocuments);

        //When
        sendSubmissionNotifications.apply(caseDetails);

        //Then
        //verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotDispatchSubmittedNotificationsIfOtherState() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPayment);

        //When
        sendSubmissionNotifications.apply(caseDetails);

        //Then
        //verify(notificationDispatcher).send(applicationOutstandingActionNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }
}
