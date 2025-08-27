package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.notification.DssNotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_CY;

@ExtendWith(MockitoExtension.class)
class DssApplicationReceivedNotificationTest {

    private static final String CASE_NUMBER = "CN1";

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private DssNotificationHelper dssNotificationHelper;

    @InjectMocks
    private DssApplicationReceivedNotification dssApplicationReceivedNotification;

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithEmail() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setSubjectEmailAddress("subject@outlook.com");
        dssCaseData.setLanguagePreference(ENGLISH);
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getSubjectEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED);
        assertThat(dssCaseData.getSubjectNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithWelshEmail() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setSubjectEmailAddress("subject@outlook.com");
        dssCaseData.setLanguagePreference(WELSH);
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getSubjectEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED_CY);
        assertThat(dssCaseData.getSubjectNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setRepresentativeFullName("Rep Full Name");
        dssCaseData.setRepresentativeEmailAddress("rep@outlook.com");
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToRepresentative(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getRepresentativeEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED);
        assertThat(dssCaseData.getRepNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    private DssCaseData getMockDssCaseData() {
        return DssCaseData.builder()
            .subjectFullName("Subject Full Name")
            .notifyPartyMessage("Message for the party")
            .build();
    }

    private NotificationResponse getMockNotificationResponse() {
        return NotificationResponse.builder()
            .status("Success")
            .build();
    }
}
