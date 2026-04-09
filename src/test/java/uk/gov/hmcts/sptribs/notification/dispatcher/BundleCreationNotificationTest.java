package uk.gov.hmcts.sptribs.notification.dispatcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.notification.NotificationHelper;
import uk.gov.hmcts.sptribs.notification.NotificationServiceCIC;
import uk.gov.hmcts.sptribs.notification.TemplateName;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class BundleCreationNotificationTest {

    @Mock
    private NotificationServiceCIC notificationService;

    @Mock
    private NotificationHelper notificationHelper;

    @InjectMocks
    private BundleCreatedNotification bundleCreatedNotification;

    @Test
    void shouldNotifyApplicantThatBundleIsCreated() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setApplicantEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getApplicantCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

        bundleCreatedNotification.sendToApplicant(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getApplicantEmailAddress()),
            anyMap(),
            eq(TemplateName.BUNDLE_CREATED_EMAIL));
    }

    @Test
    void shouldNotifyRepresentativeThatBundleIsCreated() {
        //Given
        final CaseData data = getMockCaseData();
        data.getCicCase().setRepresentativeEmailAddress("testrepr@outlook.com");

        //When
        when(notificationHelper.buildEmailNotificationRequest(any(), anyMap(), any(TemplateName.class)))
            .thenReturn(NotificationRequest.builder().build());
        when(notificationHelper.getRepresentativeCommonVars(any(), any(CaseData.class))).thenReturn(new HashMap<>());

        bundleCreatedNotification.sendToRepresentative(data, TEST_CASE_ID.toString());

        //Then
        verify(notificationService).sendEmail(any(NotificationRequest.class), eq(TEST_CASE_ID.toString()));
        verify(notificationHelper).buildEmailNotificationRequest(
            eq(data.getCicCase().getRepresentativeEmailAddress()),
            anyMap(),
            eq(TemplateName.BUNDLE_CREATED_EMAIL));
    }

    private CaseData getMockCaseData() {
        CicCase cicCase = CicCase.builder()
            .fullName("fullName").caseNumber(TEST_CASE_ID.toString())
            .build();

        return CaseData.builder().cicCase(cicCase).build();
    }
}
