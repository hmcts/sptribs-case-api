package uk.gov.hmcts.sptribs.citizen.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_PARTY_INFO;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED;
import static uk.gov.hmcts.sptribs.notification.TemplateName.APPLICATION_RECEIVED_CY;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class DssApplicationReceivedNotificationTest {

    private static final String CASE_NUMBER = TEST_CASE_ID.toString();
    private static final String CICA_REFERENCE_NUMBER = "X/12/123456-TM1A";

    private static final EditCicaCaseDetails CICA_CASE_DETAILS = EditCicaCaseDetails.builder()
            .cicaReferenceNumber(CICA_REFERENCE_NUMBER).build();
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
        templateVars.put(HAS_CICA_NUMBER, false);
        templateVars.put(CICA_REF_NUMBER, "");

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getSubjectEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED);
        assertThat(dssCaseData.getSubjectNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    @Test
    void shouldNotifySubjectOfApplicationReceivedWithCicaReferenceInEmail() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setSubjectEmailAddress("subject@outlook.com");
        dssCaseData.setLanguagePreference(ENGLISH);
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(CICA_CASE_DETAILS).build();
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        templateVars.put(HAS_CICA_NUMBER, true);
        templateVars.put(CICA_REF_NUMBER, CICA_REFERENCE_NUMBER);

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
                .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getSubjectCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER));
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
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToSubject(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getSubjectEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED_CY);
        assertThat(dssCaseData.getSubjectNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
    }

    @Test
    void shouldNotifyRepresentativeOfApplicationReceivedWithCicaReferenceInEmail() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        dssCaseData.setRepresentativeFullName("Rep Full Name");
        dssCaseData.setRepresentativeEmailAddress("rep@outlook.com");
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(CICA_CASE_DETAILS).build();
        final NotificationResponse notificationResponse = getMockNotificationResponse();

        Map<String, Object> templateVars = new HashMap<>();
        templateVars.put(CIC_CASE_REPRESENTATIVE_NAME, dssCaseData.getRepresentativeFullName());
        templateVars.put(CONTACT_PARTY_INFO, dssCaseData.getNotifyPartyMessage());
        templateVars.put(HAS_CICA_NUMBER, true);
        templateVars.put(CICA_REF_NUMBER, CICA_REFERENCE_NUMBER);

        when(dssNotificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(dssNotificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(templateVars);
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToRepresentative(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER));
        verify(dssNotificationHelper).buildEmailNotificationRequest(
            dssCaseData.getRepresentativeEmailAddress(),
            templateVars,
            APPLICATION_RECEIVED);
        assertThat(dssCaseData.getRepNotificationResponse().getStatus()).isEqualTo(notificationResponse.getStatus());
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
        when(notificationService.sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER))).thenReturn(notificationResponse);

        dssApplicationReceivedNotification.sendToRepresentative(caseData, CASE_NUMBER);

        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(CASE_NUMBER));
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
