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
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;

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
        final CaseData caseData = new CaseData();
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setSubjectEmailAddress("subject@outlook.com");
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(DssCaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(dssCaseData, CASE_NUMBER, caseData);

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getSubjectEmailAddress(),
            templateVars,
            TemplateName.APPLICATION_RECEIVED);
        assertThat(dssCaseData.getSubjectNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithEmail() {
        final CaseData caseData = new CaseData();
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setRepresentativeFullName("Rep Full Name");
        dssCaseData.setRepresentativeEmailAddress("rep@outlook.com");
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getRepresentativeCommonVars(any(), any(DssCaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToRepresentative(dssCaseData, CASE_NUMBER, caseData);

        verify(notificationService).sendEmailNew(any(NotificationRequest.class), any(), any(CaseData.class));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getRepresentativeEmailAddress(),
            templateVars,
            TemplateName.APPLICATION_RECEIVED);
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
