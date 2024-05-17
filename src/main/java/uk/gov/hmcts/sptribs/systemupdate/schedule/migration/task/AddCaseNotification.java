package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.NotificationResponse;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;
import uk.gov.hmcts.sptribs.model.CaseNotification;

import java.util.List;

import static java.util.Objects.isNull;

public class AddCaseNotification implements CaseTask {
    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {
        CaseData caseData = caseDetails.getData();
        CicCase cicCase = caseData.getCicCase();
        final NotificationResponse subjectResponse = cicCase.getSubjectNotifyList(); // why is it called this? - should probably change the name of this
        final NotificationResponse representativeResponse = cicCase.getRepNotificationResponse();
        final NotificationResponse applicantResponse = cicCase.getAppNotificationResponse();
        final NotificationResponse respondentResponse = cicCase.getResNotificationResponse();
        List<CaseNotification> notifications = caseData.getCaseNotifications();

        // Apply -> add case notifications to returned case
        if (!isNull(subjectResponse)) {
            CaseNotification subjectNotification = CaseNotification.builder()
                .sentAt(subjectResponse.getUpdatedAtTime())
                .reference(subjectResponse.getClientReference())
                .emailAddress("")
                .notificationType(subjectResponse.getNotificationType())
                .status(subjectResponse.getStatus())
                .build();
            notifications.add(subjectNotification);
        }

        if(!isNull(representativeResponse)) {
            CaseNotification representativeNotification = CaseNotification.builder()
                .sentAt(representativeResponse.getUpdatedAtTime())
                .reference(representativeResponse.getClientReference())
                .emailAddress("")
                .notificationType(representativeResponse.getNotificationType())
                .status(representativeResponse.getStatus())
                .build();
            notifications.add(representativeNotification);
        }

        if ((!isNull(applicantResponse))) {
            CaseNotification representativeNotification = CaseNotification.builder()
                .sentAt(representativeResponse.getUpdatedAtTime())
                .reference(representativeResponse.getClientReference())
                .emailAddress("")
                .notificationType(representativeResponse.getNotificationType())
                .status(representativeResponse.getStatus())
                .build();
            notifications.add(representativeNotification);
        }

        if ((!isNull(respondentResponse))) {
            CaseNotification respondentNotification = CaseNotification.builder()
                .sentAt(respondentResponse.getUpdatedAtTime())
                .reference(respondentResponse.getClientReference())
                .emailAddress("")
                .notificationType(respondentResponse.getNotificationType())
                .status(respondentResponse.getStatus())
                .build();
            notifications.add(respondentNotification);
        }

        caseData.setNotifications(notifications);

        // Apply -> set just sent notification to false
        caseData.setSendNotification(false);


        return caseDetails;
    }
}
