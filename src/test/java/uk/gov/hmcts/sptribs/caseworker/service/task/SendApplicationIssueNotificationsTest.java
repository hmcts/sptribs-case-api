package uk.gov.hmcts.sptribs.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseInvite;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.notification.ApplicationIssuedNotification;
import uk.gov.hmcts.sptribs.notification.NotificationDispatcher;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SendApplicationIssueNotificationsTest {

    @Mock
    private ApplicationIssuedNotification applicationIssuedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @InjectMocks
    private SendApplicationIssueNotifications underTest;

    @Test
    void shouldSendAllNotifications() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("Spain").build());
        caseData.setCaseInvite(new CaseInvite("applicant2Invite@email.com", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
    }


    @Test
    void shouldSendOverseasNotificationIfPersonalService() {
        CaseData caseData = caseData();
        caseData.setApplicationType(SOLE_APPLICATION);
        caseData.getApplicant2().setAddress(AddressGlobalUK.builder().country("UK").build());
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        caseData.setCaseInvite(new CaseInvite("applicant2Invite@email.com", null, null));
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).build();

        underTest.apply(caseDetails);

        verify(notificationDispatcher).send(applicationIssuedNotification, caseData, caseDetails.getId());
    }
}
