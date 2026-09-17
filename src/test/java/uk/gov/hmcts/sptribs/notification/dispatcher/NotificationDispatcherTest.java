package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationParties;
import uk.gov.hmcts.sptribs.common.service.ContactPartiesService;
import uk.gov.hmcts.sptribs.notification.PartiesNotification;
import uk.gov.hmcts.sptribs.notification.model.NotificationContext;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.APPLICANT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.REPRESENTATIVE;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.RESPONDENT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.SUBJECT;
import static uk.gov.hmcts.sptribs.ciccase.model.NotificationParties.TRIBUNAL;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private ContactPartiesService contactPartiesService;

    @Mock
    private PartiesNotification notification;

    @InjectMocks
    private NotificationDispatcher notificationDispatcher;

    private static final String CASE_REFERENCE = "1616-5914-0147-3378";
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
    }

    @Test
    void shouldDoNothingWhenCorrespondencePartiesIsEmpty() {
        NotificationContext context = buildContext(Set.of(), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verifyNoInteractions(notification, contactPartiesService);
    }

    @Test
    void shouldDoNothingWhenCorrespondencePartiesIsNull() {
        NotificationContext context = buildContext(null, null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verifyNoInteractions(notification, contactPartiesService);
    }

    @Test
    void shouldSendToSubjectWithoutDocuments() {
        NotificationContext context = buildContext(Set.of(SUBJECT), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToSubject(caseData, CASE_REFERENCE);
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldSendToSubjectWithDocuments() {
        Map<String, String> documents = Map.of("test", "test2");
        NotificationContext context = buildContext(Set.of(SUBJECT), documents);

        when(notification.sendToSubject(caseData, CASE_REFERENCE, documents)).thenReturn("TEST_ID");

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToSubject(caseData, CASE_REFERENCE, documents);
        assertThat(context.getCorrespondenceIDs()).contains("TEST_ID");
        verify(contactPartiesService).linkCorrespondenceIdsToDocuments(
            caseData, documents, context.getCorrespondenceIDs());
    }

    @Test
    void shouldSendToRespondentWithoutDocuments() {
        NotificationContext context = buildContext(Set.of(RESPONDENT), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToRespondent(caseData, CASE_REFERENCE);
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldSendToRespondentWithDocuments() {
        Map<String, String> documents = Map.of("doc1", "url1");
        NotificationContext context = buildContext(Set.of(RESPONDENT), documents);

        when(notification.sendToRespondent(caseData, CASE_REFERENCE, documents)).thenReturn("TEST_ID");

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToRespondent(caseData, CASE_REFERENCE, documents);
        assertThat(context.getCorrespondenceIDs()).contains("TEST_ID");
        verify(contactPartiesService).linkCorrespondenceIdsToDocuments(caseData, documents, context.getCorrespondenceIDs());
    }

    @Test
    void shouldSendToApplicantWithoutDocuments() {
        NotificationContext context = buildContext(Set.of(APPLICANT), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToApplicant(caseData, CASE_REFERENCE);
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldSendToApplicantWithDocuments() {
        Map<String, String> documents = Map.of("doc1", "url1");
        NotificationContext context = buildContext(Set.of(APPLICANT), documents);

        when(notification.sendToApplicant(caseData, CASE_REFERENCE, documents)).thenReturn("TEST_ID");

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToApplicant(caseData, CASE_REFERENCE, documents);
        assertThat(context.getCorrespondenceIDs()).contains("TEST_ID");
        verify(contactPartiesService).linkCorrespondenceIdsToDocuments(caseData, documents, context.getCorrespondenceIDs());
    }

    @Test
    void shouldSendToRepresentativeWithoutDocuments() {
        NotificationContext context = buildContext(Set.of(REPRESENTATIVE), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToRepresentative(caseData, CASE_REFERENCE);
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldSendToRepresentativeWithDocuments() {
        Map<String, String> documents = Map.of("doc1", "url1");
        NotificationContext context = buildContext(Set.of(REPRESENTATIVE), documents);

        when(notification.sendToRepresentative(caseData, CASE_REFERENCE, documents)).thenReturn("TEST_ID");

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToRepresentative(caseData, CASE_REFERENCE, documents);
        assertThat(context.getCorrespondenceIDs()).contains("TEST_ID");
        verify(contactPartiesService).linkCorrespondenceIdsToDocuments(caseData, documents, context.getCorrespondenceIDs());
    }

    @Test
    void shouldSendToTribunal() {
        NotificationContext context = buildContext(Set.of(TRIBUNAL), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToTribunal(caseData, CASE_REFERENCE);
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldAddPartyLabelToErrorsWhenNotificationThrows() {
        NotificationContext context = buildContext(Set.of(SUBJECT), null);

        doThrow(new RuntimeException("send failed")).when(notification).sendToSubject(any(CaseData.class), anyString());

        notificationDispatcher.sendToCorrespondenceParties(context);

        assertThat(context.getErrors()).containsExactly(SUBJECT.getLabel());
        verifyNoInteractions(contactPartiesService);
    }

    @Test
    void shouldContinueSendingToOtherPartiesWhenOneThrows() {
        NotificationContext context = buildContext(Set.of(SUBJECT, RESPONDENT), null);

        doThrow(new RuntimeException("send failed")).when(notification).sendToSubject(any(CaseData.class), anyString());

        notificationDispatcher.sendToCorrespondenceParties(context);

        assertThat(context.getErrors()).containsExactly(SUBJECT.getLabel());
        verify(notification).sendToRespondent(caseData, CASE_REFERENCE);
    }

    @Test
    void shouldSendToAllPartiesWhenAllPresent() {
        NotificationContext context = buildContext(Set.of(SUBJECT, RESPONDENT, APPLICANT, REPRESENTATIVE, TRIBUNAL), null);

        notificationDispatcher.sendToCorrespondenceParties(context);

        verify(notification).sendToSubject(caseData, CASE_REFERENCE);
        verify(notification).sendToRespondent(caseData, CASE_REFERENCE);
        verify(notification).sendToApplicant(caseData, CASE_REFERENCE);
        verify(notification).sendToRepresentative(caseData, CASE_REFERENCE);
        verify(notification).sendToTribunal(caseData, CASE_REFERENCE);
        assertThat(context.getErrors()).isEmpty();
    }


    private NotificationContext buildContext(Set<NotificationParties> parties, Map<String, String> documents) {
        return NotificationContext.builder()
            .correspondenceParties(parties)
            .caseData(caseData)
            .caseReference(CASE_REFERENCE)
            .notification(notification)
            .uploadedDocuments(documents)
            .build();
    }
}
